package nexusvault.pack.index;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.pack.PackHeader;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class IndexFileReader {

	private final static int SIGNATURE_INDEX = ('P' << 24) | ('A' << 16) | ('C' << 8) | 'K';
	private final static int SIGNATURE_AIDX = ('A' << 24) | ('I' << 16) | ('D' << 8) | 'X';

	public IndexFileReader() {

	}

	public IndexFile read(final BinaryReader reader) {
		if ((reader == null) || !reader.isOpen()) {
			throw new IllegalArgumentException();
		}

		final IndexFile indexFile = new IndexFile();

		loadHeader(indexFile, reader);
		loadPackHeader(indexFile, reader);
		loadAIDX(indexFile, reader);

		buildFileTree(indexFile, reader);

		return indexFile;
	}

	protected void loadHeader(IndexFile indexFile, BinaryReader reader) {
		reader.seek(Seek.BEGIN, 0);

		final StructIndexFileHeader header = new StructIndexFileHeader();
		indexFile.header = header;

		header.signature = reader.readInt32(); // o:4
		header.version = reader.readInt32(); // o:8

		if (header.signature != SIGNATURE_INDEX) {
			throw new SignatureMismatchException("Index file", SIGNATURE_INDEX, header.signature);
		}

		if (header.version != 1) {
			throw new VersionMismatchException("Index file", 1, header.version);
		}

		reader.seek(Seek.CURRENT, 512); // o:520
		header.fileSize = reader.readInt64(); // 520 - 528
		reader.seek(Seek.CURRENT, 8); // 528 - 536
		header.offsetPackHeaders = reader.readInt64(); // 536 - 544
		header.packHeaderCount = reader.readUInt32(); // 544 - 552
		reader.seek(Seek.CURRENT, 4);
		header.rootPackHeaderIndex = reader.readInt64(); // 552 - 560
		reader.seek(Seek.CURRENT, 16); // 560 - 576
	}

	protected void loadPackHeader(IndexFile indexFile, BinaryReader reader) {

		if (indexFile.header.offsetPackHeaders < 0) {
			throw new IllegalArgumentException("Index File: Pack header offset: index overflow");
		}

		if ((indexFile.header.packHeaderCount > Integer.MAX_VALUE) || (indexFile.header.packHeaderCount < 0)) {
			throw new IllegalArgumentException(
					String.format("Index File : Number of pack headers (%d) exceed integer range", indexFile.header.packHeaderCount));
		}

		reader.seek(Seek.BEGIN, indexFile.header.offsetPackHeaders);
		indexFile.packHeader = new PackHeader[(int) indexFile.header.packHeaderCount];
		for (int i = 0; i < indexFile.packHeader.length; ++i) {
			indexFile.packHeader[i] = new PackHeader(reader.readInt64(), reader.readInt64());
		}
	}

	protected void loadAIDX(IndexFile indexFile, BinaryReader reader) {
		if (indexFile.header.rootPackHeaderIndex > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					String.format("Index File : Number of pack headers (%d) exceed integer range", indexFile.header.rootPackHeaderIndex));
		}

		final PackHeader header = indexFile.packHeader[(int) indexFile.header.rootPackHeaderIndex];
		reader.seek(Seek.BEGIN, header.getOffset());
		final AIDX rootBlock = new AIDX(reader.readInt32(), reader.readInt32(), reader.readInt32(), reader.readInt32());
		if (rootBlock.signature != SIGNATURE_AIDX) {
			throw new SignatureMismatchException("Index file: AIDX block", SIGNATURE_INDEX, rootBlock.signature);
		}
		indexFile.aidx = rootBlock;
	}

	protected void buildFileTree(IndexFile indexFile, BinaryReader reader) {
		final IdxDirectory rootDirectory = new IdxDirectory(null, "", indexFile.aidx.rootPackHeaderIdx);
		indexFile.rootDirectory = rootDirectory;

		final Queue<IdxDirectory> fringe = new LinkedList<>();
		fringe.add(rootDirectory);

		while (!fringe.isEmpty()) {
			final IdxDirectory directory = fringe.poll();
			loadDirectory(indexFile, reader, directory);
			for (final IdxDirectory subDirs : directory.getSubDirectories()) {
				fringe.add(subDirs);
			}
		}
	}

	protected void loadDirectory(IndexFile indexFile, BinaryReader reader, IdxDirectory parent) {
		final PackHeader packheader = indexFile.getPackHeader(parent.packHeaderIdx);
		reader.seek(Seek.BEGIN, packheader.getOffset());

		final long numSubDirectories = reader.readUInt32();
		final long numFiles = reader.readUInt32();
		final String nameTwine = extractNames(reader, packheader.getSize(), numSubDirectories, numFiles);

		final List<IdxEntry> entries = new ArrayList<>();

		for (int i = 0; i < numSubDirectories; ++i) {
			final long subDirNameOffset = reader.readUInt32();
			final long subDirectoryHeaderIdx = reader.readUInt32();

			final int nullTerminator = nameTwine.indexOf(0, (int) subDirNameOffset);
			final String subDirName = nameTwine.substring((int) subDirNameOffset, nullTerminator);

			final IdxDirectory directory = new IdxDirectory(parent, subDirName, subDirectoryHeaderIdx);
			entries.add(directory);
		}

		for (long i = 0; i < numFiles; ++i) {
			final long fileNameOffset = reader.readUInt32(); // o: 4
			final int flags = reader.readInt32(); // o: 8
			final long writeTime = reader.readInt64(); // o: 16
			final long uncompressedSize = reader.readInt64(); // o: 24
			final long compressedSize = reader.readInt64(); // o: 32
			final byte[] hash = new byte[20];
			reader.readInt8(hash, 0, hash.length);
			final int unk1 = reader.readInt32(); // o: 56 // Prob. filled with trash data

			final int nullTerminator = nameTwine.indexOf(0, (int) fileNameOffset);
			final String fileName = nameTwine.substring((int) fileNameOffset, nullTerminator);

			final IdxFileLink file = new IdxFileLink(parent, fileName, flags, writeTime, uncompressedSize, compressedSize, hash, unk1);
			entries.add(file);
		}

		parent.childs = entries;
	}

	protected String extractNames(BinaryReader reader, long blockSize, long numSubDirectories, long numFiles) {
		final long dataPosition = reader.getPosition();
		final long nameOffset = (numSubDirectories * StructIdxDirectoryHeader.SIZE_IN_BYTES) + (numFiles * StructIdxFileHeader.SIZE_IN_BYTES);
		final long nameLengthInByte = blockSize - (2 * Integer.BYTES) - nameOffset;
		reader.seek(Seek.CURRENT, nameOffset);
		final byte[] entryNameAsBytes = new byte[(int) nameLengthInByte];
		reader.readInt8(entryNameAsBytes, 0, entryNameAsBytes.length);
		final String nameTwine = new String(entryNameAsBytes, Charset.forName("UTF8"));
		reader.seek(Seek.BEGIN, dataPosition);
		return nameTwine;
	}

}
