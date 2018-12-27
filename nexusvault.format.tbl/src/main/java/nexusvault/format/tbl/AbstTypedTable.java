package nexusvault.format.tbl;

import java.lang.reflect.Field;

import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.exception.InvalidStructInstanceException;

abstract class AbstTypedTable<T> implements GameTable<T>, Iterable<T> {

	protected final Class<T> entryClass;
	private final StructFactory entryFactory;

	public AbstTypedTable(Class<T> entryClass) {
		if (entryClass == null) {
			throw new IllegalArgumentException();
		}
		this.entryClass = entryClass;
		this.entryFactory = StructFactory.build();
	}

	protected T buildRecord(RawTable table, int recordIdx) {
		final Field[] fields = entryClass.getFields();
		if (fields.length != table.getFieldCount()) {
			throw new IllegalStateException();
		}

		final TableRecord record = table.getEntry(recordIdx);

		final T entry = createTableEntry();

		for (int fieldIdx = 0; fieldIdx < fields.length; ++fieldIdx) {
			final Field field = fields[fieldIdx];
			final StructTableFieldHeader tableField = table.fields[fieldIdx];

			Object value = null;
			final Class<?> fieldClass = field.getType();
			switch (tableField.getFieldDataType()) {
				case BOOL:
					if (!validateAssignability(fieldClass, boolean.class, Boolean.class)) {
						throw new ClassCastException(String.format("Record[%d] Field[%d] is of type %s and can not be assigned to type %s", recordIdx, fieldIdx,
								tableField.dataType, fieldClass));
					}
					break;
				case FLOAT:
					if (!validateAssignability(fieldClass, float.class, Float.class, double.class, Double.class)) {
						throw new ClassCastException(String.format("Record[%d] Field[%d] is of type %s and can not be assigned to type %s", recordIdx, fieldIdx,
								tableField.dataType, fieldClass));
					}
					break;
				case INT32:
					if (!validateAssignability(fieldClass, int.class, Integer.class, long.class, Long.class)) {
						throw new ClassCastException(String.format("Record[%d] Field[%d] is of type %s and can not be assigned to type %s", recordIdx, fieldIdx,
								tableField.dataType, fieldClass));
					}
					break;
				case INT64:
					if (!validateAssignability(fieldClass, long.class, Long.class)) {
						throw new ClassCastException(String.format("Record[%d] Field[%d] is of type %s and can not be assigned to type %s", recordIdx, fieldIdx,
								tableField.dataType, fieldClass));
					}
					break;
				case STRING:
					if (!validateAssignability(fieldClass, String.class)) {
						throw new ClassCastException(String.format("Record[%d] Field[%d] is of type %s and can not be assigned to type %s", recordIdx, fieldIdx,
								tableField.dataType, fieldClass));
					}
					break;
				default:
					throw new IllegalStateException();
			}

			value = record.get(fieldIdx);

			try {
				final boolean fieldAccesible = field.isAccessible();
				field.setAccessible(true);
				field.set(entry, value);
				field.setAccessible(fieldAccesible);
			} catch (final IllegalArgumentException e1) {
				throw new TypeConversionException(e1);
			} catch (final IllegalAccessException e2) {
				throw new TypeNotAccessibleException(e2);
			}
		}

		return entry;
	}

	protected T createTableEntry() throws TypeNotInstantiableException {
		try {
			return entryFactory.create(entryClass);
		} catch (final InvalidStructInstanceException e) {
			throw new TypeNotInstantiableException(e);
		}
	}

	protected boolean validateAssignability(Class<?> actual, Class<?>... expectedAny) {
		for (final Class<?> acceptable : expectedAny) {
			if (actual.isAssignableFrom(acceptable)) {
				return true;
			}
		}
		return false;
	}

}
