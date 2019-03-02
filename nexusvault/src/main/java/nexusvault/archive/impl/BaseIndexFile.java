package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.shared.exception.IntegerOverflowException;

final class BaseIndexFile extends AbstractPackedFile implements IndexFile {

	@Override
	public IndexDirectoryData getDirectoryData(int packIdx) throws IOException {
		try (BinaryReader reader = getFileReader()) {
			final StructPackHeader pack = getPack(packIdx);

			reader.seek(Seek.BEGIN, pack.getOffset());

			final long numSubDirectories = reader.readUInt32();
			final long numFiles = reader.readUInt32();
			final String nameTwine = extractNames(reader, pack.getSize(), numSubDirectories, numFiles);

			if ((numSubDirectories > Integer.MAX_VALUE) || (numSubDirectories < 0)) {
				throw new IntegerOverflowException("number of sub directories");
			}
			if ((numFiles > Integer.MAX_VALUE) || (numFiles < 0)) {
				throw new IntegerOverflowException("number of file links");
			}

			final List<StructIdxDirectory> directories = new ArrayList<>((int) numSubDirectories);
			for (int i = 0; i < numSubDirectories; ++i) {
				final StructIdxDirectory dir = new StructIdxDirectory((int) reader.readUInt32(), (int) reader.readUInt32()); // structReader.read(new
																																// StructIdxDirectory(),
																																// reader);
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

	@Override
	public void setDirectoryData(int packIdx, IndexDirectoryData data) throws IOException {
		if (packIdx < this.getPackCount()) {
			overwriteDirectoryData(packIdx, data);
		} else {
			writeNewDirectoryData(packIdx, data);
		}
	}

	@Override
	public void setNumberOfExpectedEntries(int count) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
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

	private void overwriteDirectoryData(int packIdx, IndexDirectoryData data) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	private void writeNewDirectoryData(int packIdx, IndexDirectoryData data) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

}
