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
import nexusvault.archive.ArchiveMemoryException;
import nexusvault.archive.PackIndexOutOfBounds;
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

			final List<IdxDirectoryAttribute> directories = new ArrayList<>((int) numSubDirectories);
			for (int i = 0; i < numSubDirectories; ++i) {
				final int nameOffset = (int) reader.readUInt32();
				final int directoryIndex = (int) reader.readUInt32();

				if (nameOffset < 0) {
					throw new IntegerOverflowException("'nameOffset'");
				}

				if (directoryIndex < 0) {
					throw new IntegerOverflowException("'directoryIndex'");
				}

				final int nullTerminator = nameTwine.indexOf(0, nameOffset);
				final String directoryName = nameTwine.substring(nameOffset, nullTerminator);

				directories.add(new IdxDirectoryAttribute(directoryName, directoryIndex));
			}

			final List<IdxFileAttribute> fileLinks = new ArrayList<>((int) numFiles);
			for (long i = 0; i < numFiles; ++i) {
				final int nameOffset = (int) reader.readUInt32();
				final int flags = reader.readInt32();
				final long writeTime = reader.readInt64();
				final long uncompressedSize = reader.readInt64();
				final long compressedSize = reader.readInt64();
				final byte[] hash = new byte[20];
				reader.readInt8(hash, 0, hash.length);
				final int unk_034 = reader.readInt32();

				if (nameOffset < 0) {
					throw new IntegerOverflowException("'nameOffset'");
				}

				final int nullTerminator = nameTwine.indexOf(0, nameOffset);
				final String fileName = nameTwine.substring(nameOffset, nullTerminator);

				fileLinks.add(new IdxFileAttribute(fileName, flags, writeTime, uncompressedSize, compressedSize, hash, unk_034));
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
	public long writeDirectoryData(long packIdx, IndexDirectoryData data) throws IOException, PackIndexOutOfBounds {
		if (packIdx == -1) { // pack unavailable
			final StructPackHeader pack = writeNewIndexDirectory(data);
			packIdx = writeNewPack(pack);
			return packIdx;
		}

		StructPackHeader pack = getPack(packIdx);
		if (pack.offset == 0) { // directory new -> create directory, overwrite pack
			pack = writeNewIndexDirectory(data);
			overwritePack(pack, packIdx);
		} else { // directory old -> overwrite directory & pack
			pack = overwriteIndexDirectory(pack, data);
			overwritePack(pack, packIdx);
		}
		return packIdx;
	}

	@Override
	public void overwriteFileAttribute(long packIdx, int fileIndex, byte[] hash, IdxFileAttribute fileAttribute) throws IOException, PackIndexOutOfBounds {
		checkPackAvailability(packIdx);

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
			writer.writeInt32(fileAttribute.getFlags());
			writer.writeInt64(fileAttribute.getWriteTime());
			writer.writeInt64(fileAttribute.getUncompressedSize());
			writer.writeInt64(fileAttribute.getCompressedSize());
			writer.writeInt8(fileAttribute.getHash(), 0, 20);
			writer.writeInt32(fileAttribute.getUnk_034());
		}
	}

	@Override
	public void flushWrite() throws IOException {
		super.flushWrite();
	}

	private long computeDirectoryDataSize(IndexDirectoryData data) {
		long expectedSize = 2 * Integer.BYTES;

		final List<IdxDirectoryAttribute> dirs = data.getDirectories();
		expectedSize += dirs.size() * StructIdxDirectory.SIZE_IN_BYTES;
		for (final IdxDirectoryAttribute dir : dirs) {
			expectedSize += (dir.getName().length() * 1) + 1; // UTF8, max 1 byte per character
		}

		final List<IdxFileAttribute> files = data.getFileLinks();
		expectedSize += files.size() * StructIdxFile.SIZE_IN_BYTES;
		for (final IdxFileAttribute file : files) {
			expectedSize += (file.getName().length() * 1) + 1; // UTF8, max 1 byte per character
		}
		return expectedSize;
	}

	private StructPackHeader overwriteIndexDirectory(StructPackHeader pack, IndexDirectoryData data) throws IOException {
		final long expectedSize = computeDirectoryDataSize(data);

		MemoryBlock memoryBlock = findMemoryBlock(pack.offset);
		if (expectedSize > memoryBlock.size()) {
			freeMemoryBlock(memoryBlock);
			memoryBlock = allocateMemory(expectedSize);
		}

		StructPackHeader newPack;
		try (BinaryWriter writer = getFileWriter()) {
			newPack = writeIndexDirectory(writer, memoryBlock.position(), data);
		}

		if (newPack.size > memoryBlock.size()) {
			throw new ArchiveMemoryException("MemoryBlock size violation. Possible write operation outside of allocation.");
		}

		return newPack;
	}

	private StructPackHeader writeNewIndexDirectory(IndexDirectoryData data) throws IOException {
		final long expectedSize = computeDirectoryDataSize(data);
		final MemoryBlock block = allocateMemory(expectedSize);

		StructPackHeader pack;
		try (BinaryWriter writer = getFileWriter()) {
			pack = writeIndexDirectory(writer, block.position(), data);
		}

		if (pack.size > block.size()) {
			throw new ArchiveMemoryException("MemoryBlock size violation. Possible write operation outside of allocation.");
		}

		return pack;
	}

	private StructPackHeader writeIndexDirectory(BinaryWriter writer, long offset, IndexDirectoryData data) {
		writer.seek(Seek.BEGIN, offset);

		final long position = writer.getPosition();
		writer.writeInt32(data.getDirectories().size());
		writer.writeInt32(data.getFileLinks().size());

		int nameOffset = 0;

		for (final IdxDirectoryAttribute dir : data.getDirectories()) {
			final byte[] nameBytes = dir.getName().getBytes(StandardCharsets.UTF_8);

			writer.writeInt32(nameOffset);
			writer.writeInt32(dir.getDirectoryIndex());

			nameOffset += nameBytes.length + 1;
		}

		for (final IdxFileAttribute file : data.getFileLinks()) {
			final byte[] nameBytes = file.getName().getBytes(StandardCharsets.UTF_8);

			writer.writeInt32(nameOffset);
			writer.writeInt32(file.getFlags());
			writer.writeInt64(file.getWriteTime());
			writer.writeInt64(file.getUncompressedSize());
			writer.writeInt64(file.getCompressedSize());
			writer.writeInt8(file.getHash(), 0, 20);
			writer.writeInt32(file.getUnk_034());

			nameOffset += nameBytes.length + 1;
		}

		for (final IdxDirectoryAttribute dir : data.getDirectories()) {
			writeIndexName(writer, dir.getName());
		}

		for (final IdxFileAttribute file : data.getFileLinks()) {
			writeIndexName(writer, file.getName());
		}

		final long size = writer.getPosition() - position;
		return new StructPackHeader(position, size);
	}

	private void writeIndexName(BinaryWriter writer, String name) {
		final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		if (name.length() != nameBytes.length) {
			throw new IllegalStateException(
					String.format("Names which contain characters which span multiple codepoints are not supported. Invalid Name: %s", name));
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
