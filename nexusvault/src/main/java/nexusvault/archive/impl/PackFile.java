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
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

class PackFile {

	private static final int SIZE_2_MB = 2 << 20;

	private Path filePath;
	protected StructArchiveFile header;
	private List<StructPackHeader> packs;

	private ArchiveMemoryModel memoryModel;

	private FileAccessCache fileCache;
	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;
	private BinaryReader reader;
	private BinaryWriter writer;

	private boolean writeReady = false;
	private boolean initialized = false;
	private int packArrayCapacity = 0;

	private boolean autoGrow = true;
	private int growSize = 100;

	public PackFile() {

	}

	public void closeTarget() throws IOException {
		if (!initialized) {
			return;
		}
		flushPendingWrites();
		disposeResources();
		initialized = false;
	}

	public void enableWriteMode() throws IOException {
		if (writeReady) {
			return;
		}
		checkFileState();
		writeReady = true;
		try (BinaryReader reader = getFileReader()) {
			initializeWriteMode(reader);
		}
		computePackArrayCapacity();
	}

	public void flushPendingWrites() throws IOException {
		if (writeReady) {
			try (BinaryWriter writer = getFileWriter()) {
				writeMemoryLayoutToFile(writer);
			}

			try (BinaryWriter writer = getFileWriter()) {
				writePackCountToFile(writer);
				writeFileSizeToFile(writer);
				writePackOffsetToFile(writer);
			}
			// TODO
		}
	}

	public BinaryReader getFileReader() throws IOException {
		checkFileState();
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

	public BinaryWriter getFileWriter() throws IOException {
		checkFileState();
		if (writeBuffer == null) {
			writeBuffer = createByteBuffer();
		}

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
	public ArchiveMemoryModel getMemoryModel() throws IllegalStateException {
		checkWriteState();
		return memoryModel;
	}

	public StructPackHeader getPack(long packIdx) {
		checkFileState();
		return packs.get((int) packIdx);
	}

	public int getPackArrayCapacity() {
		return packArrayCapacity;
	}

	public int getPackArraySize() {
		checkFileState();
		return (int) header.packCount;
	}

	public Path getTarget() {
		return filePath;
	}

	public boolean isOpen() {
		return initialized;
	}

	public boolean isPackAvailable(long packIdx) {
		return (0 <= packIdx) && (packIdx < getPackArraySize());
	}

	public boolean isPackWritable(long packIdx) {
		return (0 <= packIdx) && (packIdx < getPackArrayCapacity());
	}

	public void openTarget(Path path) throws IOException {
		if (path == null) {
			throw new IllegalArgumentException("'path' must not be null");
		}
		if (initialized && !path.equals(filePath)) {
			throw new IllegalStateException(String.format("Target already set to %s. Close file before setting new target.", filePath));
		}
		filePath = path;

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

	public void overwritePack(BinaryWriter writer, StructPackHeader pack, long packIdx) throws IOException {
		checkFileState();
		checkIsPackArrayInitialized();

		if (!isPackAvailable(packIdx)) {
			throw new IndexOutOfBoundsException(String.format("PackIdx %d is not in use. Unable to overwrite.", packIdx)); // TODO
		}

		final long offset = header.packOffset + (packIdx * StructPackHeader.SIZE_IN_BYTES);

		writer.seek(Seek.BEGIN, offset);
		writer.writeInt64(pack.offset);
		writer.writeInt64(pack.size);
	}

	public long writeNewPack(BinaryWriter writer, StructPackHeader pack) throws IOException {
		checkFileState();
		checkIsPackArrayInitialized();

		if (header.packCount == packArrayCapacity) {
			if (autoGrow) {
				increasePackArrayCapacity(writer, packArrayCapacity + growSize);
			} else {
				throw new IllegalStateException(); // TODO
			}
		}

		final long offset = header.packOffset + (header.packCount * StructPackHeader.SIZE_IN_BYTES);
		if (offset >= (header.packOffset + (packArrayCapacity * StructPackHeader.SIZE_IN_BYTES))) {
			throw new IllegalStateException(); // TODO
		}

		writer.seek(Seek.BEGIN, offset);
		writer.writeInt64(pack.offset);
		writer.writeInt64(pack.size);

		final long idx = header.packCount;
		header.packCount += 1;
		packs.add(pack);

		if (packs.size() != header.packCount) {
			throw new IllegalStateException(); // TODO
		}

		return idx;
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

	public void setPackArrayAutoGrow(boolean value) {
		this.autoGrow = value;
	}

	public void setPackArrayAutoGrowSize(int value) {
		this.growSize = Math.min(1, value);
	}

	public void initializePackArray() throws IOException {
		setPackArrayCapacityTo(growSize);
	}

	public void setPackArrayCapacityTo(int minimalSize) throws IOException {
		checkFileState();
		try (BinaryWriter writer = getFileWriter()) {
			if (isPackArrayInitialized()) {
				increasePackArrayCapacity(writer, Math.max(getPackArrayCapacity(), minimalSize));
			} else {
				allocateInitialPackArray(writer, minimalSize);
			}
		}
	}

	public boolean isPackArrayInitialized() {
		return header.packOffset != 0;
	}

	private void buildMemoryModel(BinaryReader reader) {
		reader.seek(Seek.BEGIN, StructArchiveFile.SIZE_IN_BYTES + Long.BYTES);
		buildMemoryModel(reader, true);
	}

	private void buildMemoryModel(BinaryReader reader, boolean direction) {
		memoryModel.clear();
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
		final Path parent = getTarget().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Files.createFile(getTarget());

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
		this.packArrayCapacity = (int) header.packCount;
	}

	private long relocateMemory(BinaryWriter writer, long memOffset, long memOldSize, long memNewSize) throws IOException {
		final ArchiveMemoryModel memoryModel = getMemoryModel();
		final MemoryBlock oldBlock = memoryModel.tryFindBlockAt(memOffset);
		if (oldBlock == null) {
			throw new IllegalStateException(); // TODO
		}
		final ByteBuffer temp = ByteBuffer.allocateDirect((int) memOldSize);
		try (BinaryReader reader = getFileReader()) {
			reader.seek(Seek.BEGIN, oldBlock.position());
			reader.readTo(temp);
			temp.flip();
		}

		memoryModel.freeMemory(oldBlock);

		final MemoryBlock newBlock = memoryModel.allocateMemory(memNewSize);
		writer.seek(Seek.BEGIN, newBlock.position());
		writer.write(temp);
		return newBlock.position();
	}

	private void allocateInitialPackArray(BinaryWriter writer, int minimalCapacity) {
		if (isPackArrayInitialized()) {
			throw new IllegalStateException("Pack array already initialized");
		}

		final MemoryBlock block = getMemoryModel().allocateMemory(minimalCapacity * StructPackHeader.SIZE_IN_BYTES);
		header.packOffset = block.position();
		computePackArrayCapacity();
	}

	private void increasePackArrayCapacity(BinaryWriter writer, int newCapacity) throws IOException {
		checkIsPackArrayInitialized();

		if (newCapacity < getPackArrayCapacity()) {
			throw new IllegalArgumentException("'new capacity' must be greater or equal to current capacity");
		}

		if (newCapacity == getPackArrayCapacity()) {
			return;
		}

		final long oldSize = header.packCount * StructPackHeader.SIZE_IN_BYTES;
		final long newSize = newCapacity * StructPackHeader.SIZE_IN_BYTES;
		final long newPackOffset = relocateMemory(writer, header.packOffset, oldSize, newSize);
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

	protected void checkFileState() {
		if (!isOpen()) {
			throw new IllegalStateException("File not open"); // TODO
		}
		// TODO Auto-generated method stub

	}

	protected void disposeResources() {
		filePath = null;
		header = null;

		packs = null;
		memoryModel = null;

		packArrayCapacity = 0;

		writeReady = false;

		if (reader != null) {
			try {
				reader.close();
			} catch (final IOException e) { // ignore
			}
		}

		if (writer != null) {
			try {
				writer.close();
			} catch (final IOException e) { // ignore
			}
		}

		reader = null;
		readBuffer = null;

		writer = null;
		writeBuffer = null;

		if (fileCache != null) {
			fileCache.shutDown();
			fileCache = null;
		}
	}

	protected void readFileOnOpen(BinaryReader reader) {
		readHeader(reader);
		readAllPackHeader(reader);
	}

}
