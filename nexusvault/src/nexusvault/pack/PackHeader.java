package nexusvault.pack;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class PackHeader {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(PackHeader.class);

	@Order(1)
	@StructField(DataType.UBIT_64)
	private final long offset;

	@Order(2)
	@StructField(DataType.UBIT_64)
	private final long size;

	public PackHeader() {
		this(0, 0);
	}

	public PackHeader(long offset, long size) {
		this.offset = offset;
		this.size = size;
	}

	@Override
	public String toString() {
		return "PackDirectoryHeader [offset=" + offset + ", size=" + size + "]";
	}

	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}
}