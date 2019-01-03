package nexusvault.format.m3.v100.pointer;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.VisitableStruct;

public class ArrayTypePointer<T extends VisitableStruct> {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(ArrayTypePointer.class);

	@Order(1)
	@StructField(DataType.BIT_32)
	private int elements;

	@Order(2)
	@StructField(DataType.BIT_32)
	private int unused;

	@Order(3)
	@StructField(DataType.BIT_64)
	private long offset;

	private final Class<T> typeOf;
	private final int byteSize;

	public ArrayTypePointer(int byteSize) {
		this.typeOf = null;
		this.byteSize = byteSize;
	}

	public ArrayTypePointer(Class<T> type) {
		this.typeOf = type;
		this.byteSize = StructUtil.sizeOf(type);
	}

	public ArrayTypePointer(Class<T> type, int elements, long offset) {
		this.typeOf = type;
		this.byteSize = StructUtil.sizeOf(type);
		this.elements = elements;
		this.offset = offset;
	}

	public int getArraySize() {
		return elements;
	}

	public boolean hasElements() {
		return elements != 0;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int getElementSize() {
		return byteSize;
	}

	public Class<T> getTypeOfElement() {
		return typeOf;
	}

	public boolean hasType() {
		return typeOf != null;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ArrayTypePointer [elements=");
		builder.append(elements);
		builder.append(", unused=");
		builder.append(unused);
		builder.append(", offset=");
		builder.append(offset);
		builder.append(", typeOf=");
		builder.append(typeOf);
		builder.append(", byteSize=");
		builder.append(byteSize);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + byteSize;
		result = (prime * result) + elements;
		result = (prime * result) + (int) (offset ^ (offset >>> 32));
		result = (prime * result) + ((typeOf == null) ? 0 : typeOf.hashCode());
		result = (prime * result) + unused;
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
		final ArrayTypePointer other = (ArrayTypePointer) obj;
		if (byteSize != other.byteSize) {
			return false;
		}
		if (elements != other.elements) {
			return false;
		}
		if (offset != other.offset) {
			return false;
		}
		if (typeOf == null) {
			if (other.typeOf != null) {
				return false;
			}
		} else if (!typeOf.equals(other.typeOf)) {
			return false;
		}
		if (unused != other.unused) {
			return false;
		}
		return true;
	}

}