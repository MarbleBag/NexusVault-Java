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

package nexusvault.format.m3.debug;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

public interface FieldReader {
	Object get();

	boolean isArray();

	int size();

	Field getField();

	Object getObject();
}

final class ArrayFieldReader implements FieldReader {
	private final Field field;
	private final Object obj;
	private final Object array;
	private final int size;
	private int index;

	public ArrayFieldReader(Object obj, Field field) throws IllegalAccessException {
		this.field = field;
		this.obj = obj;
		this.array = field.get(obj);
		this.size = Array.getLength(this.array);
	}

	@Override
	public Object get() {
		final Object r = Array.get(this.array, this.index);
		this.index += 1;
		if (this.index == this.size) {
			this.index = 0;
		}
		return r;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public Object getObject() {
		return this.obj;
	}
}

final class BufferedFieldReader implements FieldReader {
	private final Field field;
	private final Object obj;
	private final Object value;

	public BufferedFieldReader(Object obj, Field field) throws IllegalAccessException {
		this.field = field;
		this.obj = obj;
		this.value = field.get(obj);
	}

	@Override
	public Object get() {
		return this.value;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public Object getObject() {
		return this.obj;
	}
}

final class DefaultFieldReader implements FieldReader {
	private final Field field;
	private final Object obj;

	public DefaultFieldReader(Object obj, Field field) {
		this.field = field;
		this.obj = obj;
	}

	@Override
	public Object get() {
		try {
			return this.field.get(this.obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public Object getObject() {
		return this.obj;
	}
}