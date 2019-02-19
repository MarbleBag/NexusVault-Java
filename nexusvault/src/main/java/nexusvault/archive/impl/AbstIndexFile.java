package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import kreed.reflection.struct.StructUtil;
import nexusvault.archive.struct.StructAIDX;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

abstract class AbstIndexFile implements IndexFile {

	protected StructArchiveFile archiveHeader;
	protected StructPackHeader[] packs;
	protected StructAIDX aidx;

	public AbstIndexFile() {

	}

	protected void initialize() throws IOException {
		final BinaryReader reader = getBinaryReader();
		this.archiveHeader = loadHeader(reader);
		this.packs = loadPackHeader(reader, archiveHeader);
		this.aidx = loadAIDX(reader, archiveHeader, packs);
	}

	@Override
	public final int getPackCount() {
		return (int) archiveHeader.packCount;
	}

	@Override
	public final int getPackRootIdx() {
		return aidx.headerIdx;
	}

	@Override
	public final StructPackHeader getPack(int packIdx) {
		return packs[packIdx];
	}

	@Override
	public IndexDirectoryData getDirectoryData(int packIdx) throws IOException {
		final BinaryReader reader = getBinaryReader();
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
																															// StructIdxDirectory(), reader);
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

	private StructArchiveFile loadHeader(BinaryReader reader) {
		final StructArchiveFile archiveHeader = StructUtil.readStruct(StructArchiveFile.class, reader, true);
		if (archiveHeader.signature != StructArchiveFile.FILE_SIGNATURE) {
			throw new SignatureMismatchException("Index file", StructArchiveFile.FILE_SIGNATURE, archiveHeader.signature);
		}
		if (archiveHeader.version != 1) {
			throw new VersionMismatchException("Index file", 1, archiveHeader.version);
		}
		return archiveHeader;
	}

	private StructPackHeader[] loadPackHeader(BinaryReader reader, StructArchiveFile archiveHeader) {
		if (archiveHeader.packOffset < 0) {
			throw new IntegerOverflowException("Index file: pack offset");
		}

		if ((archiveHeader.packCount > Integer.MAX_VALUE) || (archiveHeader.packCount < 0)) {
			throw new IntegerOverflowException("Index file: pack count");
		}

		reader.seek(Seek.BEGIN, archiveHeader.packOffset);
		final StructPackHeader[] pack = new StructPackHeader[(int) archiveHeader.packCount];
		for (int i = 0; i < pack.length; ++i) {
			pack[i] = new StructPackHeader(reader.readInt64(), reader.readInt64());
		}

		return pack;
	}

	private StructAIDX loadAIDX(BinaryReader reader, StructArchiveFile archiveHeader, StructPackHeader[] packs) {
		if (archiveHeader.packRootIdx > archiveHeader.packCount) {
			throw new IllegalArgumentException(
					String.format("Index File : Pack root idx %d exceeds pack count %d", archiveHeader.packRootIdx, archiveHeader.packCount));
		}
		if ((archiveHeader.packRootIdx > Integer.MAX_VALUE) || (archiveHeader.packRootIdx < 0)) {
			throw new IntegerOverflowException("Index file: pack root");
		}

		final StructPackHeader aidxpack = packs[(int) archiveHeader.packRootIdx];
		reader.seek(Seek.BEGIN, aidxpack.getOffset());

		final StructAIDX aidx = new StructAIDX(reader.readInt32(), reader.readInt32(), reader.readInt32(), reader.readInt32());
		if (aidx.signature != StructAIDX.SIGNATURE_AIDX) {
			throw new SignatureMismatchException("Index file: AIDX block", StructAIDX.SIGNATURE_AIDX, aidx.signature);
		}

		return aidx;
	}

	abstract protected BinaryReader getBinaryReader() throws IOException;

}