package nexusvault.pack.archive;

import nexusvault.util.ByteUtil;

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
}
