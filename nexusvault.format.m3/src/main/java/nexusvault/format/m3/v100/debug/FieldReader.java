package nexusvault.format.m3.v100.debug;

import java.lang.reflect.Field;

public interface FieldReader {
	Object get();

	boolean isArray();

	int size();

	Field getField();

	Object getObject();
}