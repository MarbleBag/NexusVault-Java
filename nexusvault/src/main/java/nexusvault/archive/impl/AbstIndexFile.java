package nexusvault.archive.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import nexusvault.archive.struct.StructAIDX;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

abstract class AbstIndexFile implements IndexFile {
	protected final StructReader<BinaryReader> structReader;
	protected StructArchiveFile archiveHeader;
	protected StructPackHeader[] packs;
	protected StructAIDX aidx;

	public AbstIndexFile() {
		final DataReadDelegator<BinaryReader> readerDelegator = DataReadDelegator.build(new kreed.reflection.struct.reader.BinaryReader());
		structReader = StructReader.build(StructFactory.build(), readerDelegator, true);
	}

	protected void initialize() {
		final BinaryReader reader = getBinaryReader();
		this.archiveHeader = loadHeader(structReader, reader);
		this.packs = loadPackHeader(structReader, reader, archiveHeader);
		this.aidx = loadAIDX(structReader, reader, archiveHeader, packs);
	}

	@Override
	public final int getPackCount() {
		return (int) archiveHeader.packCount;
	}

	@Override
	public final int getPackRootIdx() {
		return aidx.rootPackHeaderIdx;
	}

	@Override
	public final StructPackHeader getPack(int packIdx) {
		return packs[packIdx];
	}

	@Override
	public IndexDirectoryData getDirectoryData(int packIdx) {
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
			final StructIdxDirectory dir = structReader.read(new StructIdxDirectory(), reader);
			final int nullTerminator = nameTwine.indexOf(0, (int) dir.nameOffset);
			dir.name = nameTwine.substring((int) dir.nameOffset, nullTerminator);
			directories.add(dir);
		}

		final List<StructIdxFile> fileLinks = new ArrayList<>((int) numFiles);
		for (long i = 0; i < numFiles; ++i) {
			final StructIdxFile fileLink = structReader.read(new StructIdxFile(), reader);
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

	private StructArchiveFile loadHeader(StructReader<BinaryReader> structReader, BinaryReader reader) {
		final StructArchiveFile archiveHeader = structReader.read(StructArchiveFile.class, reader);
		if (archiveHeader.signature != StructArchiveFile.SIGNATURE_INDEX) {
			throw new SignatureMismatchException("Index file", StructArchiveFile.SIGNATURE_INDEX, archiveHeader.signature);
		}
		if (archiveHeader.version != 1) {
			throw new VersionMismatchException("Index file", 1, archiveHeader.version);
		}
		return archiveHeader;
	}

	private StructPackHeader[] loadPackHeader(StructReader<BinaryReader> structReader, BinaryReader reader, StructArchiveFile archiveHeader) {
		if (archiveHeader.packOffset < 0) {
			throw new IntegerOverflowException("Index file: pack offset");
		}

		if ((archiveHeader.packCount > Integer.MAX_VALUE) || (archiveHeader.packCount < 0)) {
			throw new IntegerOverflowException("Index file: pack count");
		}

		reader.seek(Seek.BEGIN, archiveHeader.packOffset);
		final StructPackHeader[] pack = new StructPackHeader[(int) archiveHeader.packCount];
		for (int i = 0; i < pack.length; ++i) {
			pack[i] = structReader.read(new StructPackHeader(), reader);
		}
		return pack;
	}

	private StructAIDX loadAIDX(StructReader<BinaryReader> structReader, BinaryReader reader, StructArchiveFile archiveHeader, StructPackHeader[] packs) {
		if (archiveHeader.packRootIdx > archiveHeader.packCount) {
			throw new IllegalArgumentException(
					String.format("Index File : Pack root idx %d exceeds pack count %d", archiveHeader.packRootIdx, archiveHeader.packCount));
		}
		if ((archiveHeader.packRootIdx > Integer.MAX_VALUE) || (archiveHeader.packRootIdx < 0)) {
			throw new IntegerOverflowException("Index file: pack root");
		}

		final StructPackHeader aidxpack = packs[(int) archiveHeader.packRootIdx];
		reader.seek(Seek.BEGIN, aidxpack.getOffset());

		final StructAIDX aidx = structReader.read(new StructAIDX(), reader);
		if (aidx.signature != StructAIDX.SIGNATURE_AIDX) {
			throw new SignatureMismatchException("Index file: AIDX block", StructAIDX.SIGNATURE_AIDX, aidx.signature);
		}

		return aidx;
	}

	abstract protected BinaryReader getBinaryReader();

}