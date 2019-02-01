package nexusvault.format.m3.v100.debug;

import java.lang.reflect.Field;

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