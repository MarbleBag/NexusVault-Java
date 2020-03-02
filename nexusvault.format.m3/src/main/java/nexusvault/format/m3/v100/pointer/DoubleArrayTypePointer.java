package nexusvault.format.m3.v100.pointer;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.VisitableStruct;

public class DoubleArrayTypePointer<A extends VisitableStruct, B extends VisitableStruct> {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(DoubleArrayTypePointer.class);

	@Order(1)
	@StructField(DataType.BIT_32)
	private int elements;

	@Order(2)
	@StructField(DataType.BIT_32)
	private int unused;

	@Order(3)
	@StructField(DataType.BIT_64)
	private long offsetA;

	@Order(4)
	@StructField(DataType.BIT_64)
	private long offsetB;

	private final Class<A> typeOfA;
	private final int byteSizeA;

	private final Class<B> typeOfB;
	private final int byteSizeB;

	public DoubleArrayTypePointer(int byteSizeA, int byteSizeB) {
		this.typeOfA = null;
		this.byteSizeA = byteSizeA;
		this.typeOfB = null;
		this.byteSizeB = byteSizeB;
	}

	public DoubleArrayTypePointer(Class<A> typeOfA, Class<B> typeOfB) {
		this.typeOfA = typeOfA;
		this.byteSizeA = StructUtil.sizeOf(this.typeOfA);
		this.typeOfB = typeOfB;
		this.byteSizeB = StructUtil.sizeOf(this.typeOfB);
	}

	public boolean hasElements() {
		return elements != 0;
	}

	public int getArraySize() {
		return elements;
	}

	public long getOffsetA() {
		return offsetA;
	}

	public void setOffsetA(long offset) {
		this.offsetA = offset;
	}

	public int getElementSizeA() {
		return byteSizeA;
	}

	public Class<A> getTypeOfElementA() {
		return typeOfA;
	}

	public boolean hasTypeA() {
		return typeOfA != null;
	}

	public long getOffsetB() {
		return offsetB;
	}

	public void setOffsetB(long offset) {
		this.offsetB = offset;
	}

	public int getElementSizeB() {
		return byteSizeB;
	}

	public Class<B> getTypeOfElementB() {
		return typeOfB;
	}

	public boolean hasTypeB() {
		return typeOfB != null;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DoubleArrayTypePointer [elements=");
		builder.append(elements);
		builder.append(", offsetA=");
		builder.append(offsetA);
		builder.append(", offsetB=");
		builder.append(offsetB);
		builder.append(", typeOfA=");
		builder.append(typeOfA);
		builder.append(", byteSizeA=");
		builder.append(byteSizeA);
		builder.append(", typeOfB=");
		builder.append(typeOfB);
		builder.append(", byteSizeB=");
		builder.append(byteSizeB);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + byteSizeA;
		result = (prime * result) + byteSizeB;
		result = (prime * result) + elements;
		result = (prime * result) + (int) (offsetA ^ (offsetA >>> 32));
		result = (prime * result) + (int) (offsetB ^ (offsetB >>> 32));
		result = (prime * result) + ((typeOfA == null) ? 0 : typeOfA.hashCode());
		result = (prime * result) + ((typeOfB == null) ? 0 : typeOfB.hashCode());
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
		final DoubleArrayTypePointer other = (DoubleArrayTypePointer) obj;
		if (byteSizeA != other.byteSizeA) {
			return false;
		}
		if (byteSizeB != other.byteSizeB) {
			return false;
		}
		if (elements != other.elements) {
			return false;
		}
		if (offsetA != other.offsetA) {
			return false;
		}
		if (offsetB != other.offsetB) {
			return false;
		}
		if (typeOfA == null) {
			if (other.typeOfA != null) {
				return false;
			}
		} else if (!typeOfA.equals(other.typeOfA)) {
			return false;
		}
		if (typeOfB == null) {
			if (other.typeOfB != null) {
				return false;
			}
		} else if (!typeOfB.equals(other.typeOfB)) {
			return false;
		}
		if (unused != other.unused) {
			return false;
		}
		return true;
	}

}