package nexusvault.format.tbl;

import java.lang.reflect.Field;

import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.exception.InvalidStructInstanceException;

/**
 * Provides a typed {@link GameTable} by reflection. The class which is used for the entries needs to have a public/protected or private constructor with no
 * arguments. The class needs to have a number of fields which is equal to the number of columns in the table (.tbl file). The order of the fields need to
 * reflect the order of columns in the table. Each field must have a type that's assignable for the tables column type. Those types can be
 * <ul>
 * <li>{@link boolean} / {@link Boolean}
 * <li>{@link float} / {@link Float} (Alternately: {@link double} / {@link Double})
 * <li>{@link int} / {@link Integer} (Alternately : {@link long} / {@link Long})
 * <li>{@link long} / {@link Long}
 * <li>{@link String}
 * </ul>
 */
abstract class AbstTypedTable<T> implements GameTable<T>, Iterable<T> {

	protected final Class<T> entryClass;
	private final StructFactory entryFactory;

	/**
	 *
	 * @param entryClass
	 */
	public AbstTypedTable(Class<T> entryClass, StructFactory entryFactory) {
		if (entryClass == null) {
			throw new IllegalArgumentException();
		}
		this.entryClass = entryClass;
		this.entryFactory = entryFactory;
	}

	public AbstTypedTable(Class<T> entryClass) {
		this(entryClass, StructFactory.build());
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
