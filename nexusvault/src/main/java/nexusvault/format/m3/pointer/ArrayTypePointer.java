package nexusvault.format.m3.pointer;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.VisitableStruct;

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

	public int getArrayLength() {
		return this.elements;
	}

	public boolean hasElements() {
		return this.elements != 0;
	}

	public long getOffset() {
		return this.offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int getSizeOfElement() {
		return this.byteSize;
	}

	public int getArraySize() {
		return getSizeOfElement() * getArrayLength();
	}

	public Class<T> getTypeOfElement() {
		return this.typeOf;
	}

	public boolean hasType() {
		return this.typeOf != null;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ArrayTypePointer [elements=");
		builder.append(this.elements);
		builder.append(", unused=");
		builder.append(this.unused);
		builder.append(", offset=");
		builder.append(this.offset);
		builder.append(", typeOf=");
		builder.append(this.typeOf);
		builder.append(", byteSize=");
		builder.append(this.byteSize);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.byteSize;
		result = prime * result + this.elements;
		result = prime * result + (int) (this.offset ^ this.offset >>> 32);
		result = prime * result + (this.typeOf == null ? 0 : this.typeOf.hashCode());
		result = prime * result + this.unused;
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
		if (this.byteSize != other.byteSize) {
			return false;
		}
		if (this.elements != other.elements) {
			return false;
		}
		if (this.offset != other.offset) {
			return false;
		}
		if (this.typeOf == null) {
			if (other.typeOf != null) {
				return false;
			}
		} else if (!this.typeOf.equals(other.typeOf)) {
			return false;
		}
		if (this.unused != other.unused) {
			return false;
		}
		return true;
	}

}