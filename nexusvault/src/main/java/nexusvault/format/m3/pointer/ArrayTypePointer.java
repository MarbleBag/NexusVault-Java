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

import java.util.Objects;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.VisitableStruct;

public class ArrayTypePointer<T extends VisitableStruct> {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(ArrayTypePointer.class);

	@Order(1)
	@StructField(DataType.BIT_64)
	private long elements;

	@Order(2)
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
		return (int) this.elements;
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
		return Objects.hash(this.byteSize, this.elements, this.offset, this.typeOf);
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
		return this.byteSize == other.byteSize && this.elements == other.elements && this.offset == other.offset && Objects.equals(this.typeOf, other.typeOf);
	}

}