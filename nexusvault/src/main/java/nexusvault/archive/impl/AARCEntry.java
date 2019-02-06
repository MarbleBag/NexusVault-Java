package nexusvault.archive.impl;

import java.util.Arrays;

public final class AARCEntry {
	public final long headerIdx;
	public final byte[] shaHash;
	public final long size;

	public AARCEntry(long blockIndex, byte[] shaHash, long byteOfContent) {
		super();
		this.headerIdx = blockIndex;
		this.size = byteOfContent;
		this.shaHash = new byte[shaHash.length];
		System.arraycopy(shaHash, 0, this.shaHash, 0, shaHash.length);
	}

	@Override
	public String toString() {
		return "AARCEntry [blockIndex=" + headerIdx + ", shaHash=" + ByteUtil.byteToHex(shaHash) + ", size=" + size + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (headerIdx ^ (headerIdx >>> 32));
		result = (prime * result) + Arrays.hashCode(shaHash);
		result = (prime * result) + (int) (size ^ (size >>> 32));
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
		final AARCEntry other = (AARCEntry) obj;
		if (headerIdx != other.headerIdx) {
			return false;
		}
		if (!Arrays.equals(shaHash, other.shaHash)) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}

}
