package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryReaderDelegate;
import kreed.io.util.BinaryWriter;
import kreed.io.util.BinaryWriterDelegate;
import kreed.io.util.Seek;
import kreed.io.util.SeekableByteChannelBinaryReader;
import kreed.io.util.SeekableByteChannelBinaryWriter;
import kreed.reflection.struct.StructUtil;
import nexusvault.archive.impl.ArchiveMemoryModel.MemoryBlock;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

class BasePackFile implements PackFile {

	private static final int SIZE_2_MB = 2 << 20;

	private Path filePath;
	protected StructArchiveFile header;
	private List<StructPackHeader> packs;

	private StructRootBlock rootElement;

	private ArchiveMemoryModel memoryModel;

	private FileAccessCache fileCache;
	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;
	private BinaryReader reader;
	private BinaryWriter writer;

	private boolean writeReady = false;
	private boolean initialized = false;
	private int packArrayCapacity = 0;

	private int growSize = 100;

	public BasePackFile() {

	}

	@Override
	public void openFile(Path path) throws IOException {
		setFile(path);

		if (initialized) {
			return;
		}
		initialized = true;

		final boolean targetDoesNotExist = !Files.exists(filePath);

		memoryModel = new ArchiveMemoryModel(StructArchiveFile.SIZE_IN_BYTES + Long.BYTES);
		writeReady = false;

		fileCache = new FileAccessCache(60000, filePath, EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE));

		if (targetDoesNotExist) {
			createMinimalFile();
		}

		readFileOnOpen();
	}

	private void setFile(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("'path' must not be null");
		}

		if (isFileOpen() && !path.equals(filePath)) {
			throw new IllegalStateException(String.format("Target already set to %s. Close file before setting new target.", filePath));
		}

		filePath = path;
	}

	@Override
	public void closeFile() throws IOException {
		if (!initialized) {
			return;
		}
		try {
			flushWrite();
		} finally {
			disposeResources();
			initialized = false;
		}
	}

	@Override
	public boolean isFileOpen() {
		return initialized;
	}

	@Override
	public Path getFile() {
		return filePath;
	}

	@Override
	public void enableWriteMode() throws IOException {
		if (writeReady) {
			return;
		}
		checkIsFileOpen();
		writeReady = true;
		try (BinaryReader reader = getFileReader()) {
			initializeWriteMode(reader);
		}
		computePackArrayCapacity();
	}

	@Override
	public void flushWrite() throws IOException {
		if (isWriteModeEnabled()) {
			try (BinaryWriter writer = getFileWriter()) {
				writeMemoryLayoutToFile(writer);
				writePackCountToFile(writer);
				writeFileSizeToFile(writer);
				writePackOffsetToFile(writer);
				writePackRootIndexToFile(writer);
				writePackRootToFile(writer);
			}
		}
	}

	@Override
	public boolean isWriteModeEnabled() {
		return writeReady;
	}

	@Override
	public BinaryReader getFileReader() throws IOException {
		checkIsFileOpen();
		if (readBuffer == null) {
			readBuffer = createByteBuffer();
		}

		if ((reader == null) || !reader.isOpen()) {
			// for some reason it is really really expensive to build those
			reader = new SeekableByteChannelBinaryReader(fileCache.getFileAccess(), readBuffer);
		}

		final BinaryReader delegate = new BinaryReaderDelegate(reader) {
			@Override
			public void close() {
				fileCache.startExpiring();
			}
		};
		return delegate;
	}

	@Override
	public BinaryWriter getFileWriter() throws IOException {
		checkIsFileOpen();
		if (writeBuffer == null) {
			writeBuffer = createByteBuffer();
		}

		fileCache.getFileAccess();
		if ((writer == null) || !writer.isOpen()) {
			// for some reason it is really really expensive to build those
			writer = new SeekableByteChannelBinaryWriter(fileCache.getFileAccess(), writeBuffer);
		}

		final BinaryWriter delegate = new BinaryWriterDelegate(writer) {
			@Override
			public void close() {
				if (writer.isOpen()) {
					writer.flush();
				}
				fileCache.startExpiring();
			}
		};
		return delegate;
	}

	/**
	 *
	 * @return
	 * @throws IllegalStateException
	 *             if this instance is not in write mode
	 * @see #enableWriteMode()
	 */
	@Override
	public ArchiveMemoryModel getMemoryModel() throws IllegalStateException {
		checkWriteState();
		return memoryModel;
	}

	@Override
	public StructPackHeader getPack(long packIdx) {
		checkIsFileOpen();
		return packs.get((int) packIdx);
	}

	@Override
	public int getPackArrayCapacity() {
		checkIsFileOpen();
		return packArrayCapacity;
	}

	@Override
	public int getPackArraySize() {
		checkIsFileOpen();
		return (int) header.packCount;
	}

	@Override
	public boolean isPackAvailable(long packIdx) {
		return (0 <= packIdx) && (packIdx < getPackArraySize());
	}

	public boolean isPackWritable(long packIdx) {
		return (0 <= packIdx) && (packIdx < getPackArrayCapacity());
	}

	@Override
	public void overwritePack(StructPackHeader pack, long packIdx) throws IndexOutOfBoundsException, IOException {
		if (pack == null) {
			throw new IllegalArgumentException("'pack' must not be null");
		}

		checkIsFileOpen();
		checkIsPackArrayInitialized();

		if (!isPackAvailable(packIdx)) {
			throw new IndexOutOfBoundsException(String.format("PackIdx %d is not in use. Unable to overwrite.", packIdx)); // TODO
		}

		final long offset = header.packOffset + (packIdx * StructPackHeader.SIZE_IN_BYTES);

		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, offset);
			writer.writeInt64(pack.offset);
			writer.writeInt64(pack.size);
		}

		packs.set((int) packIdx, pack);
	}

	@Override
	public long writeNewPack(StructPackHeader pack) throws IOException {
		if (pack == null) {
			throw new IllegalArgumentException("'pack' must not be null");
		}

		checkIsFileOpen();
		checkIsPackArrayInitialized();

		if (packs.size() != header.packCount) {
			throw new IllegalStateException(); // TODO
		}

		if (header.packCount == packArrayCapacity) {
			increasePackArrayCapacity(packArrayCapacity + growSize);
		}

		final long offset = header.packOffset + (header.packCount * StructPackHeader.SIZE_IN_BYTES);

		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, offset);
			writer.writeInt64(pack.offset);
			writer.writeInt64(pack.size);
		}

		final long idx = header.packCount;
		header.packCount += 1;
		packs.add(pack);

		return idx;
	}

	public void swapPack(long packIdx1, long packIdx2) throws IOException {
		if (!isPackAvailable(packIdx1)) {
			throw new IndexOutOfBoundsException(String.format("PackIdx1 %d is not in use. Unable to delete.", packIdx1));
		}

		if (!isPackAvailable(packIdx2)) {
			throw new IndexOutOfBoundsException(String.format("PackIdx2 %d is not in use. Unable to delete.", packIdx2));
		}

		if (packIdx1 == packIdx2) {
			return;
		}

		final StructPackHeader pack1 = packs.get((int) packIdx1);
		final StructPackHeader pack2 = packs.get((int) packIdx2);
		overwritePack(pack1, packIdx2);
		overwritePack(pack2, packIdx1);
	}

	@Override
	public PackIdxSwap deletePack(long packIdx) throws IOException {
		if (!isPackAvailable(packIdx)) {
			throw new IndexOutOfBoundsException(String.format("PackIdx %d is not in use. Unable to delete.", packIdx));
		}

		header.packCount -= 1;

		if ((packs.size() - 1) == packIdx) {
			packs.remove(packs.size() - 1);
			return null;
		} else {
			final long lastPackIdx = packs.size() - 1;
			overwritePack(getPack(lastPackIdx), packIdx);
			packs.remove((int) lastPackIdx);
			return new PackIdxSwap(lastPackIdx, packIdx);
		}
	}

	/**
	 *
	 * @see #setPackArrayCapacityTo(int)
	 */
	private void checkIsPackArrayInitialized() {
		if (!isPackArrayInitialized()) {
			throw new IllegalStateException("Pack array not initialized");
		}
	}

	@Override
	public void setPackArrayAutoGrowSize(int value) {
		if (value <= 0) {
			throw new IllegalArgumentException("'value' must be greater than 0");
		}
		growSize = value;
	}

	public void initializePackArray() throws IOException {
		setPackArrayCapacityTo(growSize);
	}

	// TODO does not allow to shrink the pack array
	@Override
	public void setPackArrayCapacityTo(int minimalSize) throws IOException {
		checkIsFileOpen();
		checkWriteState();
		if (isPackArrayInitialized()) {
			increasePackArrayCapacity(Math.max(getPackArrayCapacity(), minimalSize));
		} else {
			allocateInitialPackArray(minimalSize);
		}
	}

	@Override
	public boolean isPackArrayInitialized() {
		return header.packOffset != 0;
	}

	private void buildMemoryModel(BinaryReader reader) {
		reader.seek(Seek.BEGIN, StructArchiveFile.SIZE_IN_BYTES + Long.BYTES);
		buildMemoryModel(reader, true);
	}

	private void buildMemoryModel(BinaryReader reader, boolean direction) {
		memoryModel.clearMemoryModel();
		while (reader.getPosition() < header.fileSize) {
			final long blockGuard = reader.readInt64();
			final long blockPosition = reader.getPosition();
			final long blockSize = Math.abs(blockGuard);
			final boolean isFree = blockGuard < 0;
			final long moveNextBlock = blockSize + Long.BYTES;
			if (blockGuard == 0) { // end of archive
				break;
			}
			memoryModel.setInitialBlock(blockPosition, blockSize, isFree);
			reader.seek(Seek.CURRENT, direction ? moveNextBlock : -moveNextBlock);
		}
	}

	/**
	 * @throws IllegalStateException
	 *             if this instance is not in write state
	 * @see #enableWriteMode()
	 */
	private void checkWriteState() throws IllegalStateException {
		if (!writeReady) {
			throw new IllegalStateException("Pack file is not in write mode");
		}
	}

	private void computePackArrayCapacity() {
		if (!isPackArrayInitialized()) {
			packArrayCapacity = 0;
		} else {
			final MemoryBlock block = getMemoryModel().findBlockAt(header.packOffset);
			packArrayCapacity = block.size() / StructPackHeader.SIZE_IN_BYTES;
		}

		if (getPackArrayCapacity() < getPackArraySize()) {
			throw new IllegalStateException(); // TODO
		}
	}

	private ByteBuffer createByteBuffer() {
		return ByteBuffer.allocate(SIZE_2_MB).order(ByteOrder.LITTLE_ENDIAN);
	}

	private void createMinimalFile() throws IOException {
		final Path parent = getFile().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Files.createFile(getFile());

		try (BinaryWriter writer = getFileWriter()) {
			// create header for a minimal file
			final StructArchiveFile header = StructUtil.buildStruct(StructArchiveFile.class);
			header.signature = StructArchiveFile.FILE_SIGNATURE;
			header.version = 1;
			header.fileSize = StructArchiveFile.SIZE_IN_BYTES + Long.BYTES;
			header.packRootIdx = -1;

			writer.seek(Seek.BEGIN, 0);
			StructUtil.writeStruct(header, writer, true);
			writer.writeInt64(0); // header block guard
		}
	}

	private void initializeWriteMode(BinaryReader reader) {
		buildMemoryModel(reader); // Do this on request
	}

	private void readAllPackHeader(BinaryReader reader) {
		if (header.packOffset < 0) {
			throw new IntegerOverflowException("Archive file: pack offset");
		}

		if ((header.packCount > Integer.MAX_VALUE) || (header.packCount < 0)) {
			throw new IntegerOverflowException("Archive file: pack count");
		}

		if (header.packOffset == 0) {
			packs = new ArrayList<>();
		} else {
			reader.seek(Seek.BEGIN, header.packOffset);
			packs = new ArrayList<>((int) header.packCount);
			for (int i = 0; i < header.packCount; ++i) {
				packs.add(new StructPackHeader(reader.readInt64(), reader.readInt64()));
			}
		}
	}

	private void readFileOnOpen() throws IOException {
		try (BinaryReader reader = getFileReader()) {
			readFileOnOpen(reader);
		}
	}

	private void readFileOnOpen(BinaryReader reader) {
		readHeader(reader);
		readAllPackHeader(reader);
		readRootElement(reader);
	}

	@Override
	public StructRootBlock getRootElement() {
		checkIsFileOpen();
		return rootElement;
	}

	private void readRootElement(BinaryReader reader) {
		if (header.packRootIdx == -1) {
			return; // no root pack
		}

		if (header.packRootIdx > header.packCount) {
			throw new IllegalArgumentException(String.format("Archive File : Pack root idx %d exceeds pack count %d", header.packRootIdx, header.packCount));
		}
		if ((header.packRootIdx > Integer.MAX_VALUE) || (header.packRootIdx < 0)) {
			throw new IntegerOverflowException("Archive file: pack root");
		}

		final StructPackHeader rootPack = getPack(getPackRootIndex());
		reader.seek(Seek.BEGIN, rootPack.getOffset());

		final int signature = reader.readInt32();
		final int version = reader.readInt32();
		final int count = reader.readInt32();
		final int headerIdx = reader.readInt32();

		rootElement = new StructRootBlock(signature, version, count, headerIdx);
	}

	@Override
	public StructPackHeader writeRootElement(StructRootBlock element) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			return writeRootElement(writer, element);
		}
	}

	private StructPackHeader writeRootElement(BinaryWriter writer, StructRootBlock element) {
		if (element == null) {
			throw new IllegalArgumentException("'element' must not be null");
		}

		StructPackHeader rootPack;
		if (header.packRootIdx != -1) { // element already set
			rootPack = getPack(header.packRootIdx);
			writer.seek(Seek.BEGIN, rootPack.offset);
		} else {
			final MemoryBlock memoryBlock = getMemoryModel().allocateMemory(StructRootBlock.SIZE_IN_BYTES);
			rootPack = new StructPackHeader(memoryBlock.position(), StructRootBlock.SIZE_IN_BYTES);
			writer.seek(Seek.BEGIN, memoryBlock.position());
		}

		writer.writeInt32(element.signature);
		writer.writeInt32(element.version);
		writer.writeInt32(element.entryCount);
		writer.writeInt32(element.headerIdx);
		rootElement = element;
		return rootPack;
	}

	private void writePackRootToFile(BinaryWriter writer) {
		if (header.packRootIdx == -1) {
			return;
		}
		final StructPackHeader rootPack = getPack(header.packRootIdx);
		writer.seek(Seek.BEGIN, rootPack.offset);
		writer.writeInt32(rootElement.signature);
		writer.writeInt32(rootElement.version);
		writer.writeInt32(rootElement.entryCount);
		writer.writeInt32(rootElement.headerIdx);
	}

	@Override
	public void setPackRootIdx(long rootIdx) {
		checkIsFileOpen();
		header.packRootIdx = rootIdx;
	}

	@Override
	public long getPackRootIndex() {
		checkIsFileOpen();
		return header.packRootIdx;
	}

	private void readHeader(BinaryReader reader) {
		reader.seek(Seek.BEGIN, 0);
		final StructArchiveFile archiveHeader = StructUtil.readStruct(StructArchiveFile.class, reader, true);

		if (archiveHeader.signature != StructArchiveFile.FILE_SIGNATURE) {
			throw new SignatureMismatchException("Archive file", StructArchiveFile.FILE_SIGNATURE, archiveHeader.signature);
		}
		if (archiveHeader.version != 1) {
			throw new VersionMismatchException("Archive file", 1, archiveHeader.version);
		}

		header = archiveHeader;
		packArrayCapacity = (int) header.packCount;
	}

	private void writeHeader() throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, 0);
			StructUtil.writeStruct(header, writer, true);
		}
	}

	protected void checkIsFileOpen() {
		if (!isFileOpen()) {
			throw new IllegalStateException("File not open");
		}
	}

	private long relocateMemory(long memOffset, long memOldSize, long memNewSize) throws IOException {
		final ArchiveMemoryModel memoryModel = getMemoryModel();
		final MemoryBlock oldBlock = memoryModel.findBlockAt(memOffset);
		if (oldBlock == null) {
			throw new IllegalStateException(); // TODO
		}

		final ByteBuffer temp = ByteBuffer.allocateDirect((int) memOldSize);
		try (BinaryReader reader = getFileReader()) {
			reader.seek(Seek.BEGIN, oldBlock.position());
			reader.readTo(temp);
			temp.flip();
		}

		clearMemory(oldBlock);
		memoryModel.freeMemory(oldBlock);

		final MemoryBlock newBlock = memoryModel.allocateMemory(memNewSize);
		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, newBlock.position());
			writer.write(temp);
		}
		return newBlock.position();
	}

	public void clearMemory(MemoryBlock block) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, block.size());
			for (int i = 0; i < block.size(); ++i) {
				writer.writeInt8(0);
			}
		}
	}

	private void allocateInitialPackArray(int minimalCapacity) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			allocateInitialPackArray(writer, minimalCapacity);
		}
	}

	private void allocateInitialPackArray(BinaryWriter writer, int minimalCapacity) {
		if (isPackArrayInitialized()) {
			throw new IllegalStateException("Pack array already initialized");
		}

		final MemoryBlock block = getMemoryModel().allocateMemory(minimalCapacity * StructPackHeader.SIZE_IN_BYTES);
		header.packOffset = block.position();
		computePackArrayCapacity();
	}

	private void increasePackArrayCapacity(int newCapacity) throws IOException {
		if (newCapacity < packArrayCapacity) {
			throw new IllegalArgumentException("'new capacity' must be greater or equal to current capacity");
		}

		if (newCapacity == packArrayCapacity) {
			return;
		}

		final long oldSize = header.packCount * StructPackHeader.SIZE_IN_BYTES;
		final long newSize = newCapacity * StructPackHeader.SIZE_IN_BYTES;
		final long newPackOffset = relocateMemory(header.packOffset, oldSize, newSize);
		header.packOffset = newPackOffset;

		computePackArrayCapacity();
	}

	private void writeFileSizeToFile(BinaryWriter writer) {
		final long fileSize = writer.size();
		if (header.fileSize != fileSize) {
			header.fileSize = fileSize;
			writer.seek(Seek.BEGIN, 0x208); // fileSize offset
			writer.writeInt64(header.fileSize);
		}
	}

	private void writePackOffsetToFile(BinaryWriter writer) {
		writer.seek(Seek.BEGIN, 0x218); // pack offset
		writer.writeInt64(header.packOffset);
	}

	private void writePackRootIndexToFile(BinaryWriter writer) {
		writer.seek(Seek.BEGIN, 0x228); // pack counter
		writer.writeInt64(header.packRootIdx);
	}

	private void writeMemoryLayoutToFile(BinaryWriter writer) {
		final Collection<MemoryBlock> pendingUpdates = getMemoryModel().getMemoryToUpdate();
		for (final MemoryBlock block : pendingUpdates) {
			final long sizeToWrite = block.isFree() ? -block.size() : block.size();
			writer.seek(Seek.BEGIN, block.position() - Long.BYTES);
			writer.writeInt64(sizeToWrite); // block guard
			writer.seek(Seek.CURRENT, block.size());
			writer.writeInt64(sizeToWrite); // block guard
		}
	}

	private void writePackCountToFile(BinaryWriter writer) {
		writer.seek(Seek.BEGIN, 0x220); // pack counter
		writer.writeInt32(header.packCount);
	}

	protected void disposeResources() {
		filePath = null;

		header = null;
		packs = null;
		rootElement = null;

		memoryModel = null;

		packArrayCapacity = 0;

		writeReady = false;

		readBuffer = null;
		writeBuffer = null;

		if (reader != null) {
			try {
				reader.close();
			} catch (final Throwable e) { // ignore
			}
			reader = null;
		}

		if (writer != null) {
			try {
				writer.close();
			} catch (final Throwable e) { // ignore
			}
			writer = null;
		}

		if (fileCache != null) {
			try {
				fileCache.shutDown();
			} catch (final Throwable e) { // ignore
			}
			fileCache = null;
		}
	}

}
