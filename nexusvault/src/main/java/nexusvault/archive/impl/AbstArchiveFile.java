package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import kreed.reflection.struct.StructUtil;
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.ArchiveException;
import nexusvault.archive.ArchiveHashCollisionException;
import nexusvault.archive.struct.StructAARC;
import nexusvault.archive.struct.StructArchiveEntry;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

abstract class AbstArchiveFile implements ArchiveFile {

	protected StructArchiveFile archiveHeader;
	protected StructPackHeader[] packs;
	protected StructAARC aarc;
	protected Map<String, StructArchiveEntry> entries;

	@Override
	public final int getPackCount() {
		return (int) archiveHeader.packCount;
	}

	@Override
	public final int getPackRootIdx() {
		return aarc.headerIdx;
	}

	public final StructPackHeader getPack(int packIdx) {
		return packs[packIdx];
	}

	public final StructPackHeader getPack(byte[] hash) throws ArchiveEntryNotFoundException {
		return packs[(int) getEntry(hash).headerIdx];
	}

	public StructArchiveEntry getEntry(byte[] hash) throws ArchiveEntryNotFoundException {
		if (hash == null) {
			throw new IllegalArgumentException("'entry' must not be null");
		}
		final String key = ByteUtil.byteToHex(hash);
		final StructArchiveEntry entry = entries.get(key);

		if (entry == null) {
			throw new ArchiveEntryNotFoundException(key);
		}
		return entry;
	}

	protected void initialize() throws IOException {
		final BinaryReader reader = getBinaryReader();
		this.archiveHeader = loadHeader(reader);
		this.packs = loadPackHeader(reader, archiveHeader);
		this.aarc = loadAARC(reader, archiveHeader, packs);
		this.entries = loadArchivEntries(reader, packs, aarc);
	}

	private StructArchiveFile loadHeader(BinaryReader reader) {
		final StructArchiveFile archiveHeader = StructUtil.readStruct(StructArchiveFile.class, reader, true);
		if (archiveHeader.signature != StructArchiveFile.FILE_SIGNATURE) {
			throw new SignatureMismatchException("Archive file", StructArchiveFile.FILE_SIGNATURE, archiveHeader.signature);
		}
		if (archiveHeader.version != 1) {
			throw new VersionMismatchException("Archive file", 1, archiveHeader.version);
		}
		return archiveHeader;
	}

	private StructPackHeader[] loadPackHeader(BinaryReader reader, StructArchiveFile archiveHeader) {
		if (archiveHeader.packOffset < 0) {
			throw new IntegerOverflowException("Archive file: pack offset");
		}

		if ((archiveHeader.packCount > Integer.MAX_VALUE) || (archiveHeader.packCount < 0)) {
			throw new IntegerOverflowException("Archive file: pack count");
		}

		reader.seek(Seek.BEGIN, archiveHeader.packOffset);
		final StructPackHeader[] pack = new StructPackHeader[(int) archiveHeader.packCount];
		for (int i = 0; i < pack.length; ++i) {
			pack[i] = new StructPackHeader(reader.readInt64(), reader.readInt64());
		}

		return pack;
	}

	private StructAARC loadAARC(BinaryReader reader, StructArchiveFile archiveHeader, StructPackHeader[] packs) {
		if (archiveHeader.packRootIdx > archiveHeader.packCount) {
			throw new IllegalArgumentException(
					String.format("Archive File : Pack root idx %d exceeds pack count %d", archiveHeader.packRootIdx, archiveHeader.packCount));
		}
		if ((archiveHeader.packRootIdx > Integer.MAX_VALUE) || (archiveHeader.packRootIdx < 0)) {
			throw new IntegerOverflowException("Archive file: pack root");
		}

		final StructPackHeader aarcPack = packs[(int) archiveHeader.packRootIdx];
		reader.seek(Seek.BEGIN, aarcPack.getOffset());

		final StructAARC aarc = new StructAARC(reader.readInt32(), reader.readInt32(), reader.readInt32(), reader.readInt32());
		if (aarc.signature != StructAARC.SIGNATURE_AARC) {
			throw new SignatureMismatchException("Archive file: AIDX block", StructAARC.SIGNATURE_AARC, aarc.signature);
		}

		return aarc;
	}

	private Map<String, StructArchiveEntry> loadArchivEntries(BinaryReader reader, StructPackHeader[] packs, StructAARC aarc) {
		final StructPackHeader rootPack = packs[aarc.headerIdx];
		reader.seek(Seek.BEGIN, rootPack.getOffset());

		final List<StructArchiveEntry> entries = new ArrayList<>(aarc.entryCount);
		for (int i = 0; i < aarc.entryCount; ++i) {
			final long blockIndex = reader.readUInt32();
			final byte[] hash = new byte[20];
			reader.readInt8(hash, 0, hash.length);
			final long uncompressedSize = reader.readInt64();

			final StructArchiveEntry entry = new StructArchiveEntry(blockIndex, hash, uncompressedSize);
			entries.add(entry);
		}

		final Map<String, StructArchiveEntry> mapping = new HashMap<>();
		for (final StructArchiveEntry entry : entries) {
			final String hash = ByteUtil.byteToHex(entry.shaHash);
			if (mapping.containsKey(hash)) {
				throw new ArchiveHashCollisionException();
			}
			mapping.put(hash, entry);
		}

		return mapping;
	}

	@Override
	public BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException {
		final BinaryReader reader = getBinaryReader();
		final StructPackHeader pack = getPack(hash);

		reader.seek(Seek.BEGIN, pack.getOffset());

		if ((pack.size > Integer.MAX_VALUE) || (pack.size < 0)) {
			throw new IntegerOverflowException("Archive file: data exceeds " + Integer.MAX_VALUE + " bytes");
		}

		final ByteBuffer data = ByteBuffer.allocate((int) pack.size).order(reader.getOrder());
		final int writtenData = reader.readTo(data);
		data.flip();

		if (writtenData != data.capacity()) {
			throw new ArchiveException("Data not complete");
		}

		return new ByteBufferBinaryReader(data);
	}

	abstract protected BinaryReader getBinaryReader() throws IOException;

}