package nexusvault.pack.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.pack.PackHeader;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;
import nexusvault.util.ByteUtil;

public final class ArchiveFileReader {

	private final static int SIGNATURE_ARCHIVE = ('P' << 24) | ('A' << 16) | ('C' << 8) | 'K';
	private final static int SIGNATURE_AARC = ('A' << 24) | ('A' << 16) | ('R' << 8) | 'C';

	public ArchiveFile read(final BinaryReader reader) {
		if ((reader == null) || !reader.isOpen()) {
			throw new IllegalArgumentException();
		}

		final ArchiveFile archiveFile = new ArchiveFile();

		loadHeader(archiveFile, reader);
		loadPackHeader(archiveFile, reader);
		loadAARC(archiveFile, reader);
		loadAARCEntries(archiveFile, reader);

		return archiveFile;
	}

	protected void loadHeader(ArchiveFile archiveFile, BinaryReader reader) {
		reader.seek(Seek.BEGIN, 0);

		final StructArchiveFileHeader header = new StructArchiveFileHeader();
		archiveFile.header = header;

		header.signature = reader.readInt32(); // o:4
		header.version = reader.readInt32(); // o:8

		if (header.signature != SIGNATURE_ARCHIVE) {
			throw new SignatureMismatchException("Archive file", SIGNATURE_ARCHIVE, header.signature);
		}

		if (header.version != 1) {
			throw new VersionMismatchException("Archive file", 1, header.version);
		}

		reader.seek(Seek.CURRENT, 512); // o:520
		header.fileSize = reader.readInt64(); // 520 - 528
		reader.seek(Seek.CURRENT, 8); // 528 - 536
		header.offsetPackHeaders = reader.readInt64(); // 536 - 544
		header.numPackHeaders = reader.readInt64(); // 544 - 552
		header.rootPackHeaderIndex = reader.readInt64(); // 552 - 560
		reader.seek(Seek.CURRENT, 16); // 560 - 576
	}

	protected void loadPackHeader(ArchiveFile archiveFile, BinaryReader reader) {

		if (archiveFile.header.offsetPackHeaders < 0) {
			throw new IllegalArgumentException("Archive File: Pack header offset: index overflow");
		}

		if (archiveFile.header.numPackHeaders > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					String.format("Archive File : Number of pack headers (%d) exceed integer range", archiveFile.header.numPackHeaders));
		}

		reader.seek(Seek.BEGIN, archiveFile.header.offsetPackHeaders);
		archiveFile.packHeader = new PackHeader[(int) archiveFile.header.numPackHeaders];
		for (int i = 0; i < archiveFile.packHeader.length; ++i) {
			archiveFile.packHeader[i] = new PackHeader(reader.readInt64(), reader.readInt64());
		}
	}

	protected void loadAARC(ArchiveFile archiveFile, BinaryReader reader) {
		if (archiveFile.header.rootPackHeaderIndex > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					String.format("Archive File : Number of pack headers (%d) exceed integer range", archiveFile.header.rootPackHeaderIndex));
		}

		final PackHeader header = archiveFile.packHeader[(int) archiveFile.header.rootPackHeaderIndex];
		reader.seek(Seek.BEGIN, header.getOffset());
		final AARC rootBlock = new AARC(reader.readInt32(), reader.readInt32(), reader.readInt32(), reader.readInt32());
		if (rootBlock.signature != SIGNATURE_AARC) {
			throw new SignatureMismatchException("Archive file: AARC block", SIGNATURE_AARC, rootBlock.signature);
		}
		archiveFile.aarc = rootBlock;
	}

	protected void loadAARCEntries(ArchiveFile archiveFile, BinaryReader reader) {
		final PackHeader header = archiveFile.getRootHeader();
		reader.seek(Seek.BEGIN, header.getOffset());

		final List<AARCEntry> entries = new ArrayList<>(archiveFile.aarc.entryCount);

		for (int i = 0; i < archiveFile.aarc.entryCount; ++i) {
			final long blockIndex = reader.readUInt32();
			final byte[] hash = new byte[20];
			reader.readInt8(hash, 0, hash.length);
			final long uncompressedSize = reader.readInt64();
			final AARCEntry entry = new AARCEntry(blockIndex, hash, uncompressedSize);
			entries.add(entry);
		}

		archiveFile.entries = new HashMap<>();
		for (final AARCEntry entry : entries) {
			final String hash = ByteUtil.byteToHex(entry.shaHash);
			if (archiveFile.entries.containsKey(hash)) {
				throw new ArchiveHashCollisionException();
			}
			archiveFile.entries.put(hash, entry);
		}
	}

}
