package nexusvault.pack.archive;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class AARC {
	public final int SIZE_IN_BYTES = StructUtil.sizeOf(AARC.class);

	@StructField(DataType.BIT_32)
	public final int signature;
	@StructField(DataType.BIT_32)
	public final int version;
	@StructField(DataType.BIT_32)
	public final int entryCount;
	@StructField(DataType.BIT_32)
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
}
