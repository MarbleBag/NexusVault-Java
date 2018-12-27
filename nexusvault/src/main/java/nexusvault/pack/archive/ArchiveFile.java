package nexusvault.pack.archive;

import java.util.Map;

import nexusvault.pack.PackHeader;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.util.ByteUtil;

public final class ArchiveFile {

	protected StructArchiveFileHeader header;
	protected PackHeader[] packHeader;
	protected AARC aarc;
	protected Map<String, AARCEntry> entries;

	/**
	 *
	 * @param hash
	 * @return
	 * @throws ArchiveEntryNotFoundException
	 *             if there is no entry that matches the given hash
	 */
	public AARCEntry getEntry(byte[] hash) throws ArchiveEntryNotFoundException {
		if (hash == null) {
			throw new IllegalArgumentException("'entry' must not be null");
		}
		final String key = ByteUtil.byteToHex(hash);
		final AARCEntry entry = entries.get(key);

		if (entry == null) {
			throw new ArchiveEntryNotFoundException(key);
		}
		return entry;
	}

	public PackHeader getRootHeader() {
		return packHeader[aarc.headerIdx];
	}

	public PackHeader getPackHeader(AARCEntry entry) {
		if (entry == null) {
			throw new IllegalArgumentException("'entry' must not be null");
		}
		final long idx = entry.headerIdx;
		if ((idx < 0) || (idx > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException();
		}
		return packHeader[(int) idx];
	}

}
