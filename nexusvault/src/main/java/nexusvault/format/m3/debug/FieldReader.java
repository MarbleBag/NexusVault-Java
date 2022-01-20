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
		return value;
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
		return field;
	}

	@Override
	public Object getObject() {
		return obj;
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
			return field.get(obj);
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
		return field;
	}

	@Override
	public Object getObject() {
		return obj;
	}
}