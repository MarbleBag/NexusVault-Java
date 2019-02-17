package nexusvault.archive.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class StructPackHeader {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructPackHeader.class);

	@Order(1)
	@StructField(DataType.UBIT_64)
	public long offset;

	@Order(2)
	@StructField(DataType.UBIT_64)
	public long size;

	@Override
	public String toString() {
		return "StructPackHeader [offset=" + offset + ", size=" + size + "]";
	}

	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (offset ^ (offset >>> 32));
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
		final StructPackHeader other = (StructPackHeader) obj;
		if (offset != other.offset) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}

}