package nexusvault.archive.struct;

import java.util.Objects;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructPackHeader {

	static {
		if (StructUtil.sizeOf(StructRootBlock.class) != 0x10) {
			throw new StructException();
		}
	}

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
		return "StructPackHeader [offset=" + this.offset + ", size=" + this.size + "]";
	}

	public long getOffset() {
		return this.offset;
	}

	public long getSize() {
		return this.size;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.offset, this.size);
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
		return this.offset == other.offset && this.size == other.size;
	}

}