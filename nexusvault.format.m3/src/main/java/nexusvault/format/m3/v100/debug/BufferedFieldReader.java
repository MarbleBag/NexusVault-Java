package nexusvault.format.m3.v100.debug;

import java.lang.reflect.Field;

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