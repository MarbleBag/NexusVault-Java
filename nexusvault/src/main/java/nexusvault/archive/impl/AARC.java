package nexusvault.archive.impl;

import kreed.reflection.struct.StructUtil;

public final class AARC {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(AARC.class);

	public final int signature;
	public final int version;
	public final int entryCount;
	public final int headerIdx;

	public AARC(int signature, int version, int entryCount, int headerIdx) {
		super();
		this.signature = signature;
		this.version = version;
		this.entryCount = entryCount;
		this.headerIdx = headerIdx;
	}

	@Override
	public String toString() {
		return "AARC [signature=" + signature + ", version=" + version + ", entryCount=" + entryCount + ", headeridx=" + headerIdx + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + entryCount;
		result = (prime * result) + headerIdx;
		result = (prime * result) + signature;
		result = (prime * result) + version;
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
		final AARC other = (AARC) obj;
		if (entryCount != other.entryCount) {
			return false;
		}
		if (headerIdx != other.headerIdx) {
			return false;
		}
		if (signature != other.signature) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}

}
