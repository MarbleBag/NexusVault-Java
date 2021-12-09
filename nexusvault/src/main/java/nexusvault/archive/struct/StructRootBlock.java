package nexusvault.archive.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public class StructRootBlock {

	static {
		if (StructUtil.sizeOf(StructRootBlock.class) != 0x10) {
			throw new StructException();
		}
	}

	public static final int SIGNATURE_AIDX = 'A' << 24 | 'I' << 16 | 'D' << 8 | 'X';
	public static final int SIGNATURE_AARC = 'A' << 24 | 'A' << 16 | 'R' << 8 | 'C';

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructRootBlock.class);

	@StructField(DataType.BIT_32)
	public int signature;

	@StructField(DataType.BIT_32)
	public int version;

	@StructField(DataType.BIT_32)
	public int entryCount;

	@StructField(DataType.BIT_32)
	public int headerIdx;

	public StructRootBlock() {

	}

	public StructRootBlock(int signature, int version, int entryCount, int headerIdx) {
		this.signature = signature;
		this.version = version;
		this.entryCount = entryCount;
		this.headerIdx = headerIdx;
	}

	@Override
	public String toString() {
		return "StructRootBlock [signature=" + this.signature + ", version=" + this.version + ", entryCount=" + this.entryCount + ", headeridx="
				+ this.headerIdx + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.entryCount;
		result = prime * result + this.headerIdx;
		result = prime * result + this.signature;
		result = prime * result + this.version;
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
		final StructRootBlock other = (StructRootBlock) obj;
		if (this.entryCount != other.entryCount) {
			return false;
		}
		if (this.headerIdx != other.headerIdx) {
			return false;
		}
		if (this.signature != other.signature) {
			return false;
		}
		if (this.version != other.version) {
			return false;
		}
		return true;
	}

}
