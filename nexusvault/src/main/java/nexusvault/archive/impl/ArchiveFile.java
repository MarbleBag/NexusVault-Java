package nexusvault.archive.impl;

import java.util.Arrays;
import java.util.Map;

import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.shared.exception.IntegerOverflowException;

public final class ArchiveFile {

	protected StructArchiveFile header;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((aarc == null) ? 0 : aarc.hashCode());
		result = (prime * result) + ((entries == null) ? 0 : entries.hashCode());
		result = (prime * result) + ((header == null) ? 0 : header.hashCode());
		result = (prime * result) + Arrays.hashCode(packHeader);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArchiveFile other = (ArchiveFile) obj;
		if (aarc == null) {
			if (other.aarc != null) {
				return false;
			}
		} else if (!aarc.equals(other.aarc)) {
			return false;
		}
		if (entries == null) {
			if (other.entries != null) {
				return false;
			}
		} else if (!entries.equals(other.entries)) {
			return false;
		}
		if (header == null) {
			if (other.header != null) {
				return false;
			}
		} else if (!header.equals(other.header)) {
			return false;
		}
		if (!Arrays.equals(packHeader, other.packHeader)) {
			return false;
		}
		return true;
	}

}
