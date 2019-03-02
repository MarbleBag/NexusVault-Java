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
import nexusvault.archive.struct.StructAARC;
import nexusvault.archive.struct.StructAIDX;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootPackInfo;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

abstract class AbstractPackedFile {

	private static final int SIZE_2_MB = 2 << 20;

	protected StructArchiveFile header;
	protected List<StructPackHeader> packs;
	protected StructRootPackInfo rootElement;

	protected Path filePath;
	protected ArchiveMemoryModel memoryModel;
	private boolean writeReady = false;

	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;
	private BinaryReader reader;
	private BinaryWriter writer;

	private int maxPackCount;

	private FileAccessCache fileCache;

	public StructPackHeader getPack(long packIdx) {
		return packs.get((int) packIdx);
	}

	public void setTarget(Path path) throws IOException {
		this.filePath = path;
		this.memoryModel = new ArchiveMemoryModel(StructArchiveFile.SIZE_IN_BYTES + Long.BYTES);

		this.fileCache = new FileAccessCache(60000, path, EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE));

		if (Files.exists(path)) {
			readExistingFile();
		} else {
			createNewFile();
		}
	}

	public boolean isDisposed() {
		return this.filePath == null;
	}

	public void dispose() {
		this.filePath = null;
		this.header = null;
		this.rootElement = null;
		this.packs = null;
		this.memoryModel = null;

		this.maxPackCount = 0;

		try {
			if (this.reader != null) {
				this.reader.close();
			}
		} catch (final IOException e) { // ignore
		}

		try {
			if (this.writer != null) {
				this.writer.close();
			}
		} catch (final IOException e) { // ignore
		}

		this.reader = null;
		this.readBuffer = null;

		this.writer = null;
		this.writeBuffer = null;

		this.fileCache.shutDown();
		this.fileCache = null;
	}

	private void readExistingFile() throws IOException {
		try (BinaryReader reader = getFileReader()) {
			readExistingFile(reader);
		}
	}

	private void createNewFile() throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			createNewFile(writer);
		}
	}

	public final void prepareWriteMode() throws IOException {
		if (writeReady) {
			return;
		}
		writeReady = true;
		try (BinaryReader reader = getFileReader()) {
			initializeWriteMode(reader);
		}
	}

	protected void initializeWriteMode(BinaryReader reader) {
		buildMemoryModel(reader); // Do this on request
		computeMaxPackCount(reader);
	}

	protected void readExistingFile(BinaryReader reader) {
		readHeader(reader);
		readAllPackHeader(reader);
		readRootPack(reader);
	}

	protected void createNewFile(BinaryWriter writer) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	protected BinaryReader getFileReader() throws IOException {
		if (readBuffer == null) {
			readBuffer = ByteBuffer.allocate(SIZE_2_MB).order(ByteOrder.LITTLE_ENDIAN);
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

	protected BinaryWriter getFileWriter() throws IOException {
		if (writeBuffer == null) {
			writeBuffer = ByteBuffer.allocate(SIZE_2_MB).order(ByteOrder.LITTLE_ENDIAN);
		}

		if ((writer == null) || !writer.isOpen()) {
			// for some reason it is really really expensive to build those
			writer = new SeekableByteChannelBinaryWriter(fileCache.getFileAccess(), readBuffer);
		}

		final BinaryWriter delegate = new BinaryWriterDelegate(writer) {
			@Override
			public void close() {
				fileCache.startExpiring();
			}
		};
		return delegate;
	}

	private void readHeader(BinaryReader reader) {
		final StructArchiveFile archiveHeader = StructUtil.readStruct(StructArchiveFile.class, reader, true);
		if (archiveHeader.signature != StructArchiveFile.FILE_SIGNATURE) {
			throw new SignatureMismatchException("Archive file", StructArchiveFile.FILE_SIGNATURE, archiveHeader.signature);
		}
		if (archiveHeader.version != 1) {
			throw new VersionMismatchException("Archive file", 1, archiveHeader.version);
		}
		this.header = archiveHeader;
	}

	protected final void computeMaxPackCount(BinaryReader reader) {
		if (header.packOffset == 0) {
			this.maxPackCount = 0;
		} else {
			final MemoryBlock block = memoryModel.tryFindBlockAt(header.packOffset);
			if (block == null) {
				throw new IllegalStateException(); // TODO
			}
			this.maxPackCount = block.size() / StructPackHeader.SIZE_IN_BYTES;
		}
		if (this.maxPackCount < getPackCount()) {
			throw new IllegalStateException(); // TODO
		}
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

	private void readRootPack(BinaryReader reader) {
		if (header.packRootIdx > header.packCount) {
			throw new IllegalArgumentException(String.format("Archive File : Pack root idx %d exceeds pack count %d", header.packRootIdx, header.packCount));
		}
		if ((header.packRootIdx > Integer.MAX_VALUE) || (header.packRootIdx < 0)) {
			throw new IntegerOverflowException("Archive file: pack root");
		}

		final StructPackHeader rootPack = packs.get((int) header.packRootIdx);
		reader.seek(Seek.BEGIN, rootPack.getOffset());

		final int signature = reader.readInt32();
		final int version = reader.readInt32();
		final int count = reader.readInt32();
		final int headerIdx = reader.readInt32();

		if (signature == StructAARC.SIGNATURE_AARC) {
			this.rootElement = new StructAARC(signature, version, count, headerIdx);
		} else if (signature == StructAIDX.SIGNATURE_AIDX) {
			this.rootElement = new StructAIDX(signature, version, count, headerIdx);
		} else {

			final String aarcSig = SignatureMismatchException.toString(StructAARC.SIGNATURE_AARC);
			final String aidxSig = SignatureMismatchException.toString(StructAIDX.SIGNATURE_AIDX);
			final String actualSig = SignatureMismatchException.toString(signature);
			throw new SignatureMismatchException(
					String.format("Archive file: root block\" : Expected '%s' or '%s', but was '%s'", aarcSig, aidxSig, actualSig));
		}
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

	protected void updateMemoryLayout(BinaryWriter writer) {
		final Collection<MemoryBlock> pendingUpdates = memoryModel.getMemoryToUpdate();
		for (final MemoryBlock block : pendingUpdates) {
			final long sizeToWrite = block.isFree() ? block.size() : -block.size();
			writer.seek(Seek.BEGIN, block.position() - Long.BYTES);
			writer.writeInt64(sizeToWrite); // block guard
			writer.seek(Seek.CURRENT, block.size());
			writer.writeInt64(sizeToWrite); // block guard
		}
	}

	protected Path getFilePath() {
		return this.filePath;
	}

	protected long writeDataToBlock(BinaryWriter writer, MemoryBlock block, BinaryReader data) throws IOException {
		writer.seek(Seek.BEGIN, block.position());
		writer.write(data);
		return block.position();
	}

	public final int getPackCount() {
		return (int) header.packCount;
	}

	public final int getPackRootIdx() {
		return rootElement.headerIdx;
	}

	protected long writeNewPack(BinaryWriter writer, StructPackHeader structPackHeader) throws IOException {
		if (header.packOffset == 0) {
			throw new IllegalStateException(); // TODO
		}

		if (getPackCount() == maxPackCount) {
			relocatePackBlock(writer);
		}

		final long offset = header.packOffset + (header.packCount * StructPackHeader.SIZE_IN_BYTES);
		writer.seek(Seek.BEGIN, offset);
		writer.writeInt64(structPackHeader.offset);
		writer.writeInt64(structPackHeader.size);
		final long packIdx = header.packCount;

		packs.add(structPackHeader);
		header.packCount += 1;

		if (packs.size() != header.packCount) {
			throw new IllegalStateException(); // TODO
		}

		writePackCount(writer);

		return packIdx;
	}

	private void writePackCount(BinaryWriter writer) {
		writer.seek(Seek.BEGIN, 0x220); // pack counter
		writer.writeInt32(header.packCount);
	}

	private void writePackOffset(BinaryWriter writer) {
		writer.seek(Seek.BEGIN, 0x218); // pack counter
		writer.writeInt32(header.packOffset);
	}

	protected void overwritePack(BinaryWriter writer, long headerIdx, StructPackHeader pack) {
		if (header.packOffset == 0) {
			throw new IllegalStateException(); // TODO
		}

		if ((headerIdx < 0) || (getPackCount() <= headerIdx)) {
			throw new IndexOutOfBoundsException(); // TODO
		}

		final long offset = header.packOffset + (headerIdx * StructPackHeader.SIZE_IN_BYTES);
		writer.seek(Seek.BEGIN, offset);
		writer.writeInt64(pack.offset);
		writer.writeInt64(pack.size);
		packs.set((int) headerIdx, pack);
	}

	private void relocatePackBlock(BinaryWriter writer) throws IOException {
		maxPackCount = (int) (header.packCount + 100);
		header.packOffset = relocateArray(writer, header.packOffset, (int) header.packCount, maxPackCount, StructPackHeader.SIZE_IN_BYTES);
		writePackOffset(writer);
	}

	protected long relocateArray(BinaryWriter writer, long offset, int oldCount, int newCount, int sizePerElement) throws IOException {
		final MemoryBlock oldBlock = memoryModel.tryFindBlockAt(offset);
		if (oldBlock == null) {
			throw new IllegalStateException(); // TODO
		}
		final ByteBuffer temp = ByteBuffer.allocateDirect(oldCount * sizePerElement);

		try (BinaryReader reader = getFileReader()) {
			reader.seek(Seek.BEGIN, oldBlock.position());
			reader.readTo(temp);
			temp.flip();
		}

		memoryModel.freeMemory(oldBlock);

		final MemoryBlock newBlock = memoryModel.allocateMemory(newCount * sizePerElement);
		writer.seek(Seek.BEGIN, newBlock.position());
		writer.write(temp);
		updateMemoryLayout(writer);
		return newBlock.position();
	}

}
