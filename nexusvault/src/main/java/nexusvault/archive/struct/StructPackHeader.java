package nexusvault.archive.struct;

import java.util.Objects;

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

	public StructPackHeader() {

	}

	public StructPackHeader(long offset, long size) {
		this.offset = offset;
		this.size = size;
	}

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
		return Objects.hash(offset, size);
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
		return (offset == other.offset) && (size == other.size);
	}

}