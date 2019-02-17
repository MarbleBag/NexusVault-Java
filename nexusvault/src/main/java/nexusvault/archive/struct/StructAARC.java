package nexusvault.archive.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class StructAARC {

	public static final int SIGNATURE_AARC = ('A' << 24) | ('A' << 16) | ('R' << 8) | 'C';
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructAARC.class);

	@StructField(DataType.BIT_32)
	public int signature;

	@StructField(DataType.BIT_32)
	public int version;

	@StructField(DataType.BIT_32)
	public int entryCount;

	@StructField(DataType.BIT_32)
	public int headerIdx;

	@Override
	public String toString() {
		return "StructAARC [signature=" + signature + ", version=" + version + ", entryCount=" + entryCount + ", headeridx=" + headerIdx + "]";
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
		final StructAARC other = (StructAARC) obj;
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
