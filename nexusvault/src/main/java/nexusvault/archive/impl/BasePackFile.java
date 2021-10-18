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
import nexusvault.archive.PackIndexOutOfBounds;
import nexusvault.archive.PackMalformedException;
import nexusvault.archive.impl.ArchiveMemoryModel.MemoryBlock;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

final class BasePackFile implements PackFile {

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

		if (this.initialized) {
			return;
		}
		this.initialized = true;

		final boolean targetDoesNotExist = !Files.exists(this.filePath);

		this.memoryModel = new ArchiveMemoryModel(StructArchiveFile.SIZE_IN_BYTES + Long.BYTES);
		this.writeReady = false;

		this.fileCache = new FileAccessCache(60000, this.filePath, EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE));

		if (targetDoesNotExist) {
			createMinimalFile();
		}

		readFileOnOpen();
	}

	private void setFile(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("'path' must not be null");
		}

		if (isFileOpen() && !path.equals(this.filePath)) {
			throw new IllegalStateException(String.format("Target already set to %s. Close file before setting new target.", this.filePath));
		}

		this.filePath = path;
	}

	@Override
	public void closeFile() throws IOException {
		if (!this.initialized) {
			return;
		}
		try {
			flushWrite();
		} finally {
			disposeResources();
			this.initialized = false;
		}
	}

	@Override
	public boolean isFileOpen() {
		return this.initialized;
	}

	@Override
	public Path getFile() {
		return this.filePath;
	}

	@Override
	public void enableWriteMode() throws IOException {
		if (this.writeReady) {
			return;
		}

		checkIsFileOpen();

		this.writeReady = true;
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
		return this.writeReady;
	}

	@Override
	public BinaryReader getFileReader() throws IOException {
		checkIsFileOpen();

		if (this.readBuffer == null) {
			this.readBuffer = createByteBuffer();
		}

		if (this.reader == null || !this.reader.isOpen()) {
			// for some reason it is really really expensive to build those
			this.reader = new SeekableByteChannelBinaryReader(this.fileCache.getFileAccess(), this.readBuffer);
		}

		final BinaryReader delegate = new BinaryReaderDelegate(this.reader) {
			@Override
			public void close() {
				BasePackFile.this.fileCache.startExpiring();
			}
		};
		return delegate;
	}

	@Override
	public BinaryWriter getFileWriter() throws IOException {
		checkIsFileOpen();

		if (this.writeBuffer == null) {
			this.writeBuffer = createByteBuffer();
		}

		this.fileCache.getFileAccess();
		if (this.writer == null || !this.writer.isOpen()) {
			// for some reason it is really really expensive to build those
			this.writer = new SeekableByteChannelBinaryWriter(this.fileCache.getFileAccess(), this.writeBuffer);
		}

		final BinaryWriter delegate = new BinaryWriterDelegate(this.writer) {
			@Override
			public void close() {
				if (this.writer.isOpen()) {
					this.writer.flush();
				}
				BasePackFile.this.fileCache.startExpiring();
			}
		};
		return delegate;
	}

	@Override
	public ArchiveMemoryModel getMemoryModel() throws IllegalStateException {
		checkWriteState();
		return this.memoryModel;
	}

	@Override
	public StructPackHeader getPack(long packIdx) throws PackIndexOutOfBounds {
		checkIsFileOpen();
		checkIsPackAvailable(packIdx);
		return this.packs.get((int) packIdx);
	}

	@Override
	public int getPackArrayCapacity() {
		checkIsFileOpen();
		return this.packArrayCapacity;
	}

	@Override
	public int getPackArraySize() {
		checkIsFileOpen();
		return (int) this.header.packCount;
	}

	private boolean isPackAvailable(long packIdx) {
		return 0 <= packIdx && packIdx < getPackArraySize();
	}

	private void checkIsPackAvailable(long index) throws PackIndexOutOfBounds {
		checkIsPackAvailable(index, null);
	}

	private void checkIsPackAvailable(long index, String msg) throws PackIndexOutOfBounds {
		if (!isPackAvailable(index)) {
			String error = String.format("Pack index %d invalid. Must be in range of [0,%d).", index, getPackArraySize());
			if (msg != null) {
				error += " " + msg;
			}
			throw new PackIndexOutOfBounds(error);
		}
	}

	public boolean isPackWritable(long packIdx) {
		return 0 <= packIdx && packIdx < getPackArrayCapacity();
	}

	@Override
	public void overwritePack(StructPackHeader pack, long packIdx) throws PackIndexOutOfBounds, PackMalformedException, IOException {
		if (pack == null) {
			throw new IllegalArgumentException("'pack' must not be null");
		}

		checkIsFileOpen();
		checkIsPackArrayInitialized();
		checkIsPackAvailable(packIdx, "Unable to overwrite.");

		final long offset = this.header.packOffset + packIdx * StructPackHeader.SIZE_IN_BYTES;

		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, offset);
			writer.writeInt64(pack.offset);
			writer.writeInt64(pack.size);
		}

		try {
			this.packs.set((int) packIdx, pack);
		} catch (final IndexOutOfBoundsException e) {
			throw new PackMalformedException(String.format("Unable to set pack at index %d. Dataset might be corrupted", packIdx), e);
		}
	}

	@Override
	public long writeNewPack(StructPackHeader pack) throws IOException {
		if (pack == null) {
			throw new IllegalArgumentException("'pack' must not be null");
		}

		checkIsFileOpen();
		checkIsPackArrayInitialized();

		if (this.packs.size() != this.header.packCount) {
			throw new PackMalformedException(
					String.format("Number of stored packs [%d] diverges from the number of expected packs [%d]. Dataset might be corrupted", pack.size,
							this.header.packCount));
		}

		if (this.header.packCount == this.packArrayCapacity) {
			increasePackArrayCapacity(this.packArrayCapacity + this.growSize);
		}

		final long offset = this.header.packOffset + this.header.packCount * StructPackHeader.SIZE_IN_BYTES;

		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, offset);
			writer.writeInt64(pack.offset);
			writer.writeInt64(pack.size);
		}

		final long idx = this.header.packCount;
		this.header.packCount += 1;
		this.packs.add(pack);

		return idx;
	}

	public void swapPack(long packIdx1, long packIdx2) throws IOException {
		checkIsPackAvailable(packIdx1, "Unable to swap index 1.");
		checkIsPackAvailable(packIdx2, "Unable to swap index 2.");

		if (packIdx1 == packIdx2) {
			return;
		}

		final StructPackHeader pack1 = this.packs.get((int) packIdx1);
		final StructPackHeader pack2 = this.packs.get((int) packIdx2);
		overwritePack(pack1, packIdx2);
		overwritePack(pack2, packIdx1);
	}

	@Override
	public PackIdxSwap deletePack(long packIdx) throws IOException, PackIndexOutOfBounds {
		checkIsFileOpen();
		checkIsPackAvailable(packIdx, "Unable to delete");

		this.header.packCount -= 1;

		final long lastPackIdx = this.packs.size() - 1;

		if (lastPackIdx == packIdx) {
			this.packs.remove((int) lastPackIdx);
			return null;
		} else {
			overwritePack(getPack(lastPackIdx), packIdx);
			this.packs.remove((int) lastPackIdx);
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
		this.growSize = value;
	}

	public void initializePackArray() throws IOException {
		setPackArrayCapacityTo(this.growSize);
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
		return this.header.packOffset != 0;
	}

	private void buildMemoryModel(BinaryReader reader) {
		reader.seek(Seek.BEGIN, StructArchiveFile.SIZE_IN_BYTES + Long.BYTES);
		buildMemoryModel(reader, true);
	}

	private void buildMemoryModel(BinaryReader reader, boolean direction) {
		this.memoryModel.clearMemoryModel();
		while (reader.getPosition() < this.header.fileSize) {
			final long blockGuard = reader.readInt64();
			final long blockPosition = reader.getPosition();
			final long blockSize = Math.abs(blockGuard);
			final boolean isFree = blockGuard < 0;
			final long moveNextBlock = blockSize + Long.BYTES;
			if (blockGuard == 0) { // end of archive
				break;
			}
			this.memoryModel.setInitialBlock(blockPosition, blockSize, isFree);
			reader.seek(Seek.CURRENT, direction ? moveNextBlock : -moveNextBlock);
		}
	}

	/**
	 * @throws IllegalStateException
	 *             if this instance is not in write state
	 * @see #enableWriteMode()
	 */
	private void checkWriteState() throws IllegalStateException {
		if (!this.writeReady) {
			throw new IllegalStateException("Pack file is not in write mode");
		}
	}

	private void computePackArrayCapacity() {
		if (!isPackArrayInitialized()) {
			this.packArrayCapacity = 0;
		} else {
			final MemoryBlock block = getMemoryModel().findBlockAt(this.header.packOffset);
			this.packArrayCapacity = block.size() / StructPackHeader.SIZE_IN_BYTES;
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
			header.fileSize = StructArchiveFile.SIZE_IN_BYTES + Long.BYTES; // header + header block guard at the end
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
		if (this.header.packOffset < 0) {
			throw new IntegerOverflowException("Archive file: pack offset");
		}

		if (this.header.packCount > Integer.MAX_VALUE || this.header.packCount < 0) {
			throw new IntegerOverflowException("Archive file: pack count");
		}

		if (this.header.packOffset == 0) {
			this.packs = new ArrayList<>();
		} else {
			reader.seek(Seek.BEGIN, this.header.packOffset);
			this.packs = new ArrayList<>((int) this.header.packCount);
			for (int i = 0; i < this.header.packCount; ++i) {
				this.packs.add(new StructPackHeader(reader.readInt64(), reader.readInt64()));
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
		return this.rootElement;
	}

	private void readRootElement(BinaryReader reader) {
		if (this.header.packRootIdx == -1) {
			return; // no root pack
		}

		if (this.header.packRootIdx > this.header.packCount) {
			throw new IllegalArgumentException(
					String.format("Archive File : Pack root idx %d exceeds pack count %d", this.header.packRootIdx, this.header.packCount));
		}
		if (this.header.packRootIdx > Integer.MAX_VALUE || this.header.packRootIdx < 0) {
			throw new IntegerOverflowException("Archive file: pack root");
		}

		final StructPackHeader rootPack = getPack(getPackRootIndex());
		reader.seek(Seek.BEGIN, rootPack.getOffset());

		final int signature = reader.readInt32();
		final int version = reader.readInt32();
		final int count = reader.readInt32();
		final int headerIdx = reader.readInt32();

		this.rootElement = new StructRootBlock(signature, version, count, headerIdx);
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
		if (this.header.packRootIdx != -1) { // element already set
			rootPack = getPack(this.header.packRootIdx);
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
		this.rootElement = element;
		return rootPack;
	}

	private void writePackRootToFile(BinaryWriter writer) {
		if (this.header.packRootIdx == -1) {
			return;
		}
		final StructPackHeader rootPack = getPack(this.header.packRootIdx);
		writer.seek(Seek.BEGIN, rootPack.offset);
		writer.writeInt32(this.rootElement.signature);
		writer.writeInt32(this.rootElement.version);
		writer.writeInt32(this.rootElement.entryCount);
		writer.writeInt32(this.rootElement.headerIdx);
	}

	@Override
	public void setPackRootIdx(long rootIdx) {
		checkIsFileOpen();
		this.header.packRootIdx = rootIdx;
	}

	@Override
	public long getPackRootIndex() {
		checkIsFileOpen();
		return this.header.packRootIdx;
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

		this.header = archiveHeader;
		this.packArrayCapacity = (int) this.header.packCount;
	}

	private void writeHeader() throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, 0);
			StructUtil.writeStruct(this.header, writer, true);
		}
	}

	private void checkIsFileOpen() {
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
		this.header.packOffset = block.position();
		computePackArrayCapacity();
	}

	private void increasePackArrayCapacity(int newCapacity) throws IOException {
		if (newCapacity < this.packArrayCapacity) {
			throw new IllegalArgumentException("'new capacity' must be greater or equal to current capacity");
		}

		if (newCapacity == this.packArrayCapacity) {
			return;
		}

		final long oldSize = this.header.packCount * StructPackHeader.SIZE_IN_BYTES;
		final long newSize = newCapacity * StructPackHeader.SIZE_IN_BYTES;
		final long newPackOffset = relocateMemory(this.header.packOffset, oldSize, newSize);
		this.header.packOffset = newPackOffset;

		computePackArrayCapacity();
	}

	private void writeFileSizeToFile(BinaryWriter writer) {
		final long fileSize = writer.size();
		if (this.header.fileSize != fileSize) {
			this.header.fileSize = fileSize;
			writer.seek(Seek.BEGIN, 0x208); // fileSize offset
			writer.writeInt64(this.header.fileSize);
		}
	}

	private void writePackOffsetToFile(BinaryWriter writer) {
		writer.seek(Seek.BEGIN, 0x218); // pack offset
		writer.writeInt64(this.header.packOffset);
	}

	private void writePackRootIndexToFile(BinaryWriter writer) {
		writer.seek(Seek.BEGIN, 0x228); // pack counter
		writer.writeInt64(this.header.packRootIdx);
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
		writer.writeInt32(this.header.packCount);
	}

	protected void disposeResources() {
		this.filePath = null;

		this.header = null;
		this.packs = null;
		this.rootElement = null;

		this.memoryModel = null;

		this.packArrayCapacity = 0;

		this.writeReady = false;

		this.readBuffer = null;
		this.writeBuffer = null;

		if (this.reader != null) {
			try {
				this.reader.close();
			} catch (final Throwable e) { // ignore
			}
			this.reader = null;
		}

		if (this.writer != null) {
			try {
				this.writer.close();
			} catch (final Throwable e) { // ignore
			}
			this.writer = null;
		}

		if (this.fileCache != null) {
			try {
				this.fileCache.shutDown();
			} catch (final Throwable e) { // ignore
			}
			this.fileCache = null;
		}
	}

}
