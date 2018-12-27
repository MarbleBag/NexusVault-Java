package nexusvault.pack.index;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class AIDX {
	public final int SIZE_IN_BYTES = StructUtil.sizeOf(AIDX.class);

	@StructField(DataType.BIT_32)
	public final int signature;
	@StructField(DataType.BIT_32)
	public final int version;
	@StructField(DataType.BIT_32)
	public final int unknown1;
	@StructField(DataType.BIT_32)
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
}