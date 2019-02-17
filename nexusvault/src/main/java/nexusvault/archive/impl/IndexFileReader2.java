package nexusvault.archive.impl;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.archive.struct.StructIndexFile;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

@Deprecated
public final class IndexFileReader2 {

	private final static int SIGNATURE_INDEX = ('P' << 24) | ('A' << 16) | ('C' << 8) | 'K';
	private final static int SIGNATURE_AIDX = ('A' << 24) | ('I' << 16) | ('D' << 8) | 'X';

	public IndexFileReader2() {

	}

	public Index2File read(final BinaryReader reader) {
		if ((reader == null) || !reader.isOpen()) {
			throw new IllegalArgumentException();
		}

		final Index2File indexFile = null;// new IndexFile();

		// loadHeader(indexFile, reader);
		// loadPackHeader(indexFile, reader);
		// loadAIDX(indexFile, reader);
		//
		// buildFileTree(indexFile, reader);

		return indexFile;
	}

	protected void loadHeader(Index2File indexFile, BinaryReader reader) {
		reader.seek(Seek.BEGIN, 0);

		final StructIndexFile header = new StructIndexFile();
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

	protected void loadPackHeader(Index2File indexFile, BinaryReader reader) {

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

	protected void loadAIDX(Index2File indexFile, BinaryReader reader) {
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

	protected void buildFileTree(Index2File indexFile, BinaryReader reader) {
		final Base2RootIdxDirectory rootDirectory = new Base2RootIdxDirectory(indexFile.aidx.rootPackHeaderIdx);
		indexFile.rootDirectory = rootDirectory;
		rootDirectory.buildFileTree(indexFile, reader);
	}

}
