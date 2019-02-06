package nexusvault.archive.impl;

import kreed.reflection.struct.StructUtil;

public final class AIDX {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(AIDX.class);

	public final int signature;
	public final int version;
	public final int unknown1;
	public final int rootPackHeaderIdx;

	public AIDX(int signature, int version, int unknown1, int headerIdx) {
		super();
		this.signature = signature;
		this.version = version;
		this.unknown1 = unknown1;
		this.rootPackHeaderIdx = headerIdx;
	}

	@Override
	public String toString() {
		return "AIDX [signature=" + signature + ", version=" + version + ", unknown1=" + unknown1 + ", rootPackHeaderIdx=" + rootPackHeaderIdx + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + rootPackHeaderIdx;
		result = (prime * result) + signature;
		result = (prime * result) + unknown1;
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
		final AIDX other = (AIDX) obj;
		if (rootPackHeaderIdx != other.rootPackHeaderIdx) {
			return false;
		}
		if (signature != other.signature) {
			return false;
		}
		if (unknown1 != other.unknown1) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}

}