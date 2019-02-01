package nexusvault.format.m3.v100.debug;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

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
		this.size = Array.getLength(array);
	}

	@Override
	public Object get() {
		final Object r = Array.get(array, index);
		index += 1;
		if (index == size) {
			index = 0;
		}
		return r;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public Object getObject() {
		return obj;
	}
}