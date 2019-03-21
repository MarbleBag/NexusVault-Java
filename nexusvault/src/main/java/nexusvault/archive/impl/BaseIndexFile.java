package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.io.util.Seek;
import nexusvault.archive.impl.ArchiveMemoryModel.MemoryBlock;
import nexusvault.archive.struct.StructAIDX;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;

final class BaseIndexFile extends AbstractArchiveFile implements IndexFile {

	public BaseIndexFile() {
		super();
	}

	@Override
	protected void afterFileRead(boolean isFileNew) throws IOException {
		if (isFileNew) {
			enableWriteMode();

			final long emptyPack = writeNewPack(new StructPackHeader());
			final long rootDirectoryPack = writeNewPack(new StructPackHeader());

			final StructRootBlock aidx = new StructRootBlock(StructRootBlock.SIGNATURE_AIDX, 1, 0, (int) rootDirectoryPack);
			final StructPackHeader pack = writeRootElement(aidx);
			final long aidxIndex = writeNewPack(pack);
			setPackRootIdx(aidxIndex);

		} else {
			final StructRootBlock aidx = getRootElement();
			if (aidx == null) {
				throw new IllegalStateException(); // TODO
			}
			if (aidx.signature != StructAIDX.SIGNATURE_AIDX) {
				throw new SignatureMismatchException("Index file", StructAIDX.SIGNATURE_AIDX, aidx.signature);
			}
		}
	}

	@Override
	protected void beforeFileClose() throws IOException {

	}

	@Override
	public int getRootDirectoryIndex() {
		return getRootElement().headerIdx;
	}

	@Override
	public IndexDirectoryData getDirectoryData(long packIdx) throws IOException {
		final StructPackHeader pack = getPack(packIdx);
		try (BinaryReader reader = getFileReader()) {
			if (pack.offset == 0) {
				return new IndexDirectoryData(Collections.emptyList(), Collections.emptyList());
			}

			reader.seek(Seek.BEGIN, pack.getOffset());
			if (pack.size < (2 * Integer.BYTES)) {
				throw new IllegalStateException(
						String.format("Pack corrupted. Minimal size needs to be at least %d bytes. Was %d.", Integer.BYTES * 2, pack.size));
			}

			final long numSubDirectories = reader.readUInt32();
			final long numFiles = reader.readUInt32();

			final long expectedSize = (numSubDirectories * StructIdxDirectory.SIZE_IN_BYTES) + (numFiles * StructIdxFile.SIZE_IN_BYTES);
			if (pack.size < expectedSize) {
				throw new IllegalStateException(String.format("Pack corrupted. Size % does not match expected size %d.", pack.size, expectedSize));
			}

			final String nameTwine = extractNames(reader, pack.size, numSubDirectories, numFiles);

			if ((numSubDirectories > Integer.MAX_VALUE) || (numSubDirectories < 0)) {
				throw new IntegerOverflowException("number of sub directories");
			}
			if ((numFiles > Integer.MAX_VALUE) || (numFiles < 0)) {
				throw new IntegerOverflowException("number of file links");
			}

			final List<StructIdxDirectory> directories = new ArrayList<>((int) numSubDirectories);
			for (int i = 0; i < numSubDirectories; ++i) {
				final StructIdxDirectory dir = new StructIdxDirectory((int) reader.readUInt32(), (int) reader.readUInt32());
				final int nullTerminator = nameTwine.indexOf(0, dir.nameOffset);
				dir.name = nameTwine.substring(dir.nameOffset, nullTerminator);
				directories.add(dir);
			}

			final List<StructIdxFile> fileLinks = new ArrayList<>((int) numFiles);
			for (long i = 0; i < numFiles; ++i) {
				final long nameOffset = reader.readUInt32();
				final int flags = reader.readInt32();
				final long writeTime = reader.readInt64();
				final long uncompressedSize = reader.readInt64();
				final long compressedSize = reader.readInt64();
				final byte[] hash = new byte[20];
				reader.readInt8(hash, 0, hash.length);
				final int unk_034 = reader.readInt32();

				final StructIdxFile fileLink = new StructIdxFile(nameOffset, flags, writeTime, uncompressedSize, compressedSize, hash, unk_034);

				final int nullTerminator = nameTwine.indexOf(0, (int) fileLink.nameOffset);
				fileLink.name = nameTwine.substring((int) fileLink.nameOffset, nullTerminator);
				fileLinks.add(fileLink);
			}

			return new IndexDirectoryData(directories, fileLinks);
		}
	}

	private String extractNames(BinaryReader reader, long blockSize, long numSubDirectories, long numFiles) {
		final long dataPosition = reader.getPosition();
		final long nameOffset = (numSubDirectories * StructIdxDirectory.SIZE_IN_BYTES) + (numFiles * StructIdxFile.SIZE_IN_BYTES);
		final long nameLengthInByte = blockSize - (2 * Integer.BYTES) - nameOffset;
		reader.seek(Seek.CURRENT, nameOffset);
		final byte[] entryNameAsBytes = new byte[(int) nameLengthInByte];
		reader.readInt8(entryNameAsBytes, 0, entryNameAsBytes.length);
		final String nameTwine = new String(entryNameAsBytes, Charset.forName("UTF8"));
		reader.seek(Seek.BEGIN, dataPosition);
		return nameTwine;
	}

	@Override
	public long writeDirectoryData(IndexDirectoryData data) throws IOException {
		return writeDirectoryData(-1, data);
	}

	@Override
	public long writeDirectoryData(long packIdx, IndexDirectoryData data) throws IOException {
		if (isPackAvailable(packIdx)) { // pack available
			StructPackHeader pack = getPack(packIdx);
			if (pack.offset == 0) { // directory new -> create directory, overwrite pack
				pack = writeNewIndexDirectory(data);
				overwritePack(pack, packIdx);
			} else { // directory old -> overwrite directory & pack
				pack = overwriteIndexDirectory(pack, data);
				overwritePack(pack, packIdx);
			}
			return packIdx;
		} else if (packIdx == -1) { // pack unavailable
			final StructPackHeader pack = writeNewIndexDirectory(data);
			packIdx = writeNewPack(pack);
			return packIdx;
		} else {
			throw new IllegalArgumentException(""); // TODO
		}
	}

	@Override
	public void overwriteFileAttribute(long packIdx, int fileIndex, byte[] hash, StructIdxFile file) throws IOException {
		if (!isPackAvailable(packIdx)) {
			throw new IllegalArgumentException(String.format("Pack with index %d not found", packIdx));
		}

		final StructPackHeader pack = getPack(packIdx);
		if (pack.offset == 0) {
			throw new IllegalStateException(String.format("Pack with index %d not initialized", packIdx));
		}

		long fileOffset = 0;

		try (BinaryReader reader = getFileReader()) {
			reader.seek(Seek.BEGIN, pack.offset);
			final long dirCount = reader.readInt32();
			final long fileCount = reader.readInt32();

			if ((fileCount == 0) || (fileCount < fileIndex)) {
				throw new IllegalStateException();
			}

			if (fileIndex == -1) {
				for (int i = 0; i < fileCount; ++i) {
					fileOffset = pack.offset + (dirCount * StructIdxDirectory.SIZE_IN_BYTES) + (i * StructIdxFile.SIZE_IN_BYTES);
					reader.seek(Seek.BEGIN, fileOffset + 0x020);
					final byte[] fileHash = new byte[20];
					reader.readInt8(hash, 0, hash.length);
					if (Arrays.equals(hash, fileHash)) {
						break;
					}
				}

				if (fileIndex == -1) {
					throw new IllegalStateException("No file entry found with hash " + ByteUtil.byteToHex(hash)); // TODO
				}
			} else {
				fileOffset = pack.offset + (dirCount * StructIdxDirectory.SIZE_IN_BYTES) + (fileIndex * StructIdxFile.SIZE_IN_BYTES);
				reader.seek(Seek.BEGIN, fileOffset + 0x020);
				final byte[] fileHash = new byte[20];
				reader.readInt8(hash, 0, hash.length);
				if (!Arrays.equals(hash, fileHash)) {
					throw new IllegalStateException(
							String.format("File hash at %d. Expected %s, but was %s", fileIndex, ByteUtil.byteToHex(hash), ByteUtil.byteToHex(fileHash)));
				}
			}
		}

		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, fileOffset + 0x04); // +nameOffset
			writer.writeInt32(file.flags);
			writer.writeInt64(file.writeTime);
			writer.writeInt64(file.uncompressedSize);
			writer.writeInt64(file.compressedSize);
			writer.writeInt8(file.hash, 0, 20);
			writer.writeInt32(file.unk_034);
		}
	}

	@Override
	public void flushWrite() throws IOException {
		super.flushWrite();
	}

	private long computeDirectoryDataSize(IndexDirectoryData data) {
		long expectedSize = 2 * Integer.BYTES;

		final List<StructIdxDirectory> dirs = data.getDirectories();
		expectedSize += dirs.size() * StructIdxDirectory.SIZE_IN_BYTES;
		for (final StructIdxDirectory dir : dirs) {
			if (dir.name == null) {
				throw new IllegalStateException("Directory has no name");
			}
			expectedSize += (dir.name.length() * 1) + 1;
		}

		final List<StructIdxFile> files = data.getFileLinks();
		expectedSize += files.size() * StructIdxFile.SIZE_IN_BYTES;
		for (final StructIdxFile file : files) {
			if (file.name == null) {
				throw new IllegalStateException("File has no name");
			}
			expectedSize += (file.name.length() * 1) + 1;
		}
		return expectedSize;
	}

	private StructPackHeader overwriteIndexDirectory(StructPackHeader pack, IndexDirectoryData data) throws IOException {
		final long expectedSize = computeDirectoryDataSize(data);

		StructPackHeader newPack;
		MemoryBlock memoryBlock = findMemoryBlock(pack.offset);
		if (!((expectedSize < pack.size) || (expectedSize < memoryBlock.size()))) {
			freeMemoryBlock(memoryBlock);
			memoryBlock = allocateMemory(expectedSize);
		}

		try (BinaryWriter writer = getFileWriter()) {
			newPack = writeIndexDirectory(writer, memoryBlock.position(), data);
		}

		if (newPack.size > pack.size) {
			throw new IllegalStateException(); // TODO oh no
		}

		return newPack;
	}

	private StructPackHeader writeNewIndexDirectory(IndexDirectoryData data) throws IOException {
		final long expectedSize = computeDirectoryDataSize(data);
		final MemoryBlock block = allocateMemory(expectedSize);

		try (BinaryWriter writer = getFileWriter()) {
			final StructPackHeader pack = writeIndexDirectory(writer, block.position(), data);

			if (pack.size > block.size()) {
				throw new IllegalStateException(); // TODO oh no
			}

			return pack;
		}
	}

	private StructPackHeader writeIndexDirectory(BinaryWriter writer, long offset, IndexDirectoryData data) {
		writer.seek(Seek.BEGIN, offset);

		final long position = writer.getPosition();
		writer.writeInt32(data.getDirectories().size());
		writer.writeInt32(data.getFileLinks().size());

		int nameOffset = 0;

		for (final StructIdxDirectory dir : data.getDirectories()) {
			dir.nameOffset = nameOffset;
			final String name = dir.name;
			if (name == null) {
				throw new IllegalStateException("Directory has no name");
			}
			final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
			nameOffset += nameBytes.length + 1;

			writer.writeInt32(dir.nameOffset);
			writer.writeInt32(dir.directoryIndex);
		}

		for (final StructIdxFile file : data.getFileLinks()) {
			file.nameOffset = nameOffset;
			final String name = file.name;
			if (name == null) {
				throw new IllegalStateException("File has no name");
			}
			final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
			nameOffset += nameBytes.length + 1;

			writer.writeInt32(file.nameOffset);
			writer.writeInt32(file.flags);
			writer.writeInt64(file.writeTime);
			writer.writeInt64(file.uncompressedSize);
			writer.writeInt64(file.compressedSize);
			writer.writeInt8(file.hash, 0, 20);
			writer.writeInt32(file.unk_034);
		}

		for (final StructIdxDirectory dir : data.getDirectories()) {
			writeIndexName(writer, dir.name);
		}

		for (final StructIdxFile file : data.getFileLinks()) {
			writeIndexName(writer, file.name);
		}

		final long size = writer.getPosition() - position;
		return new StructPackHeader(position, size);
	}

	private void writeIndexName(BinaryWriter writer, String name) {
		final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		if (name.length() != nameBytes.length) {
			throw new IllegalStateException(); // TODO Shit hits the fan again
		}
		writer.writeInt8(nameBytes, 0, nameBytes.length);
		writer.writeInt8(0); // 0 terminated
	}

	@Override
	public int getDirectoryCount() {
		return getPackArraySize();
	}

	@Override
	public void setEstimatedNumberForWriteEntries(int count) throws IOException {
		if (count < 0) {
			throw new IllegalArgumentException("'count' must be greater than or equal 0");
		}
		packFile.setPackArrayMinimalCapacity(count + 2);
	}

}
