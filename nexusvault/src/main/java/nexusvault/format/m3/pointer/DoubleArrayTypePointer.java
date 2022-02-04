/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.m3.pointer;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.VisitableStruct;

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
		return this.elements != 0;
	}

	public int getArraySize() {
		return this.elements;
	}

	public long getOffsetA() {
		return this.offsetA;
	}

	public void setOffsetA(long offset) {
		this.offsetA = offset;
	}

	public int getElementSizeA() {
		return this.byteSizeA;
	}

	public Class<A> getTypeOfElementA() {
		return this.typeOfA;
	}

	public boolean hasTypeA() {
		return this.typeOfA != null;
	}

	public long getOffsetB() {
		return this.offsetB;
	}

	public void setOffsetB(long offset) {
		this.offsetB = offset;
	}

	public int getElementSizeB() {
		return this.byteSizeB;
	}

	public Class<B> getTypeOfElementB() {
		return this.typeOfB;
	}

	public boolean hasTypeB() {
		return this.typeOfB != null;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DoubleArrayTypePointer [elements=");
		builder.append(this.elements);
		builder.append(", offsetA=");
		builder.append(this.offsetA);
		builder.append(", offsetB=");
		builder.append(this.offsetB);
		builder.append(", typeOfA=");
		builder.append(this.typeOfA);
		builder.append(", byteSizeA=");
		builder.append(this.byteSizeA);
		builder.append(", typeOfB=");
		builder.append(this.typeOfB);
		builder.append(", byteSizeB=");
		builder.append(this.byteSizeB);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.byteSizeA;
		result = prime * result + this.byteSizeB;
		result = prime * result + this.elements;
		result = prime * result + (int) (this.offsetA ^ this.offsetA >>> 32);
		result = prime * result + (int) (this.offsetB ^ this.offsetB >>> 32);
		result = prime * result + (this.typeOfA == null ? 0 : this.typeOfA.hashCode());
		result = prime * result + (this.typeOfB == null ? 0 : this.typeOfB.hashCode());
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
		final DoubleArrayTypePointer other = (DoubleArrayTypePointer) obj;
		if (this.byteSizeA != other.byteSizeA) {
			return false;
		}
		if (this.byteSizeB != other.byteSizeB) {
			return false;
		}
		if (this.elements != other.elements) {
			return false;
		}
		if (this.offsetA != other.offsetA) {
			return false;
		}
		if (this.offsetB != other.offsetB) {
			return false;
		}
		if (this.typeOfA == null) {
			if (other.typeOfA != null) {
				return false;
			}
		} else if (!this.typeOfA.equals(other.typeOfA)) {
			return false;
		}
		if (this.typeOfB == null) {
			if (other.typeOfB != null) {
				return false;
			}
		} else if (!this.typeOfB.equals(other.typeOfB)) {
			return false;
		}
		if (this.unused != other.unused) {
			return false;
		}
		return true;
	}

}