package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

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

	private TreeMap<Integer, StructPackHeader> pendingPacks;
	private int estimatedNumberOfPacks;

	public BaseIndexFile() {
		super();
	}

	@Override
	protected void afterFileRead(boolean isFileNew) throws IOException {
		pendingPacks = new TreeMap<>();

		if (isFileNew) {
			enableWriteMode();

			final long emptyPack = writeNewPack(new StructPackHeader());
			final long rootDirectoryPack = writeNewPack(new StructPackHeader());

			final StructRootBlock aidx = new StructRootBlock(StructRootBlock.SIGNATURE_AIDX, 1, 0, (int) rootDirectoryPack);
			final StructPackHeader pack = writeRootElement(aidx);
			final long aidxIndex = writeNewPack(pack);
			setPackRootIdx(aidxIndex);

		} else {
			final StructRootBlock aidx = getAIDXElement();
			if (aidx.signature != StructAIDX.SIGNATURE_AIDX) {
				throw new SignatureMismatchException("Index file", StructAIDX.SIGNATURE_AIDX, aidx.signature);
			}
		}
	}

	@Override
	protected void beforeFileClose() throws IOException {
		flushWrite();
	}

	private void storePendingPack(long packIdx, StructPackHeader pack) {
		if (!hasPendingPack(packIdx) && (packIdx != pendingPacks.size())) {
			throw new IndexOutOfBoundsException(String.format("Pack index out of bounds [%d,%d]", packFile.getPackArraySize(), getDirectoryCount()));
		}
		pendingPacks.put(Integer.valueOf((int) packIdx), pack);
	}

	private long storePendingPack(StructPackHeader pack) {
		final long packIdx = packFile.getPackArraySize() + pendingPacks.size();
		pendingPacks.put(Integer.valueOf((int) packIdx), pack);
		return packIdx;
	}

	private StructPackHeader getPendingPack(long packIdx) {
		return pendingPacks.get(Integer.valueOf((int) packIdx));
	}

	private boolean hasPendingPack(long packIdx) {
		return pendingPacks.containsKey(Integer.valueOf((int) packIdx));
	}

	private StructRootBlock getAIDXElement() {
		final StructRootBlock root = packFile.getRootElement();
		if (root == null) {
			throw new IllegalStateException(); // TODO
		}
		return root;
	}

	@Override
	public int getRootDirectoryIndex() {
		return getAIDXElement().headerIdx;
	}

	@Override
	public IndexDirectoryData getDirectoryData(long packIdx) throws IOException {
		StructPackHeader pack;
		if (packFile.isPackAvailable(packIdx)) {
			pack = packFile.getPack(packIdx);
		} else if (hasPendingPack(packIdx)) {
			pack = getPendingPack(packIdx);
		} else {
			throw new IllegalStateException(); // TODO
		}

		try (BinaryReader reader = packFile.getFileReader()) {

			if (pack.offset == 0) {
				return null;
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
		if (packFile.isPackAvailable(packIdx)) { // pack available
			StructPackHeader pack = packFile.getPack(packIdx);
			if (pack.offset == 0) { // directory new -> create directory, overwrite pack
				pack = writeNewIndexDirectory(data);
				packFile.overwritePack(pack, packIdx);
			} else { // directory old -> overwrite directory & pack
				try (BinaryWriter writer = packFile.getFileWriter()) {
					throw new UnsupportedOperationException("Not implemented yet"); // TODO
				}
			}
			return packIdx;
		} else if (hasPendingPack(packIdx)) { // pack pending
			StructPackHeader pack = getPendingPack(packIdx);
			if (pack.offset == 0) { // directory new -> create directory, overwrite pack
				pack = writeNewIndexDirectory(data);
				storePendingPack(packIdx, pack);
			} else { // directory old -> overwrite directory & pack
				try (BinaryWriter writer = packFile.getFileWriter()) {
					throw new UnsupportedOperationException("Not implemented yet"); // TODO
				}
			}
			return packIdx;
		} else if (packIdx == -1) { // pack unavailable
			final StructPackHeader pack = writeNewIndexDirectory(data);
			packIdx = storePendingPack(pack);
			return packIdx;
		} else {
			throw new IllegalArgumentException(""); // TODO
		}
	}

	@Override
	public void flushWrite() throws IOException {
		final int size = Math.max(estimatedNumberOfPacks, pendingPacks.size());

		if (packFile.getPackArrayCapacity() < size) {
			packFile.setPackArrayCapacityTo(size);
		}

		for (final Entry<Integer, StructPackHeader> pendingPack : pendingPacks.entrySet()) {
			final long expectedPackIndex = pendingPack.getKey().longValue();
			if (packFile.isPackAvailable(expectedPackIndex)) {
				packFile.overwritePack(pendingPack.getValue(), expectedPackIndex);
			} else {
				final long actualPackIndex = packFile.writeNewPack(pendingPack.getValue());
				if (expectedPackIndex != actualPackIndex) {
					throw new IllegalStateException(); // TODO something did go wrong
				}
			}
		}
		pendingPacks.clear();
	}

	private long computeDirectoryDataSize(IndexDirectoryData data) {
		long expectedSize = 2 * Integer.BYTES;

		final List<StructIdxDirectory> dirs = data.getDirectories();
		expectedSize += dirs.size() * StructIdxDirectory.SIZE_IN_BYTES;
		for (final StructIdxDirectory dir : dirs) {
			expectedSize += (dir.name.length() * 1) + 1;
		}

		final List<StructIdxFile> files = data.getFileLinks();
		expectedSize += files.size() * StructIdxFile.SIZE_IN_BYTES;
		for (final StructIdxFile file : files) {
			expectedSize += (file.name.length() * 1) + 1;
		}
		return expectedSize;
	}

	private StructPackHeader writeNewIndexDirectory(IndexDirectoryData data) throws IOException {
		try (BinaryWriter writer = packFile.getFileWriter()) {
			return writeNewIndexDirectory(writer, data);
		}
	}

	private StructPackHeader writeNewIndexDirectory(BinaryWriter writer, IndexDirectoryData data) throws IOException {
		final long expectedSize = computeDirectoryDataSize(data);
		final MemoryBlock block = packFile.getMemoryModel().allocateMemory(expectedSize);
		final StructPackHeader pack = writeNewIndexDirectory(writer, block, data);

		if (pack.size > block.size()) {
			throw new IllegalStateException(); // TODO oh no
		}

		return pack;
	}

	private StructPackHeader writeNewIndexDirectory(BinaryWriter writer, MemoryBlock block, IndexDirectoryData data) {
		writer.seek(Seek.BEGIN, block.position());

		final long position = writer.getPosition();
		writer.writeInt32(data.getDirectories().size());
		writer.writeInt32(data.getFileLinks().size());

		int nameOffset = 0;

		for (final StructIdxDirectory dir : data.getDirectories()) {
			dir.nameOffset = nameOffset;
			final String name = dir.name;
			final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
			nameOffset += nameBytes.length + 1;

			writer.writeInt32(dir.nameOffset);
			writer.writeInt32(dir.directoryHeaderIdx);
		}

		for (final StructIdxFile file : data.getFileLinks()) {
			file.nameOffset = nameOffset;
			final String name = file.name;
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
		return packFile.getPackArraySize() + pendingPacks.size();
	}

	@Override
	public void setEstimatedNumberForWriteEntries(int count) throws IOException {
		if (count <= 0) {
			throw new IllegalArgumentException("'count' must be greater than 0");
		}
		this.estimatedNumberOfPacks = count;
	}

}
