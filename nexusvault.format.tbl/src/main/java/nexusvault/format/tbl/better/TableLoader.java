package nexusvault.format.tbl.better;

import java.lang.reflect.Field;

import kreed.reflection.struct.exception.InvalidStructInstanceException;
import nexusvault.format.tbl.TypeConversionException;
import nexusvault.format.tbl.TypeNotAccessibleException;
import nexusvault.format.tbl.TypeNotInstantiableException;
import nexusvault.format.tbl.struct.DataType;

public class TableLoader {

	public <T> Field[] validateAssignability(Class<T> recordType, DataType[] types) {
		final Field[] fields = recordType.getFields();
		if (fields.length != types.length) {
			throw new TypeConversionException(String.format("Number of fields (%d) in type '%s' does not match the number of expected columns (%d)",
					fields.length, recordType.getSimpleName(), types.length));
		}

		for (int i = 0; i < fields.length; ++i) {
			final var field = fields[i];
			final var actualType = field.getType();
			boolean isValid = false;

			switch (types[i]) {
				case BOOL:
					isValid = canBeAssignedTo(actualType, boolean.class, Boolean.class);
					break;
				case FLOAT:
					isValid = canBeAssignedTo(actualType, float.class, Float.class, double.class, Double.class);
					break;
				case INT32:
					isValid = canBeAssignedTo(actualType, Integer.class, long.class, Long.class);
					break;
				case INT64:
					isValid = canBeAssignedTo(actualType, long.class, Long.class);
					break;
				case STRING:
					isValid = canBeAssignedTo(actualType, String.class);
					break;
				default:
					break;
			}

			if (!isValid) {
				throw new ClassCastException(String.format("Column [#%d] is of type '%s' and can't be assigned to Field[#%d, %s] of type '%s'", i, types[i], i,
						field.getName(), actualType));
			}
		}

		return fields;
	}

	public <T> T buildRecord(Class<T> recordType, Field[] fields, Object[] data) {
		final T entry = createTableEntry(recordType);

		for (var i = 0; i < fields.length; ++i) {
			final var field = fields[i];
			try {
				final var fieldAccesible = field.isAccessible();
				field.setAccessible(true);
				field.set(entry, data[i]);
				field.setAccessible(fieldAccesible);
			} catch (final IllegalArgumentException e1) {
				throw new TypeConversionException(e1);
			} catch (final IllegalAccessException e2) {
				throw new TypeNotAccessibleException(e2);
			}
		}

		return entry;
	}

	protected <T> T createTableEntry(Class<T> clazz) throws TypeNotInstantiableException {
		try {
			return null; // entryFactory.create(clazz);
		} catch (final InvalidStructInstanceException e) {
			throw new TypeNotInstantiableException(e);
		}
	}

	protected boolean canBeAssignedTo(Class<?> actual, Class<?>... expectedAny) {
		for (final Class<?> acceptable : expectedAny) {
			if (actual.isAssignableFrom(acceptable)) {
				return true;
			}
		}
		return false;
	}

}
