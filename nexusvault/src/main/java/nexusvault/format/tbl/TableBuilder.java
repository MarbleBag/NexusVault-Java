package nexusvault.format.tbl;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Can be used to construct a specific type out of a {@link Table}
 *
 * @param <T>
 */
public class TableBuilder<T> {

	@Retention(RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface TableField {
	}

	protected final Class<T> entryClass;
	protected final Field[] useableFields;
	protected final Constructor<T> entryConstructor;
	protected final boolean useConstructorInjection;

	public TableBuilder(Class<T> entryClass, Column[] columns) {
		this.entryClass = entryClass;
		this.useableFields = resolveClassFields(this.entryClass);
		validateAssignability(this.useableFields, columns);

		final var parameters = Arrays.stream(this.useableFields).map(Field::getType).toArray(Class[]::new);
		var constructor = searchMatchingConstructor(entryClass, parameters);
		this.useConstructorInjection = constructor != null;

		if (constructor == null) {
			try {
				constructor = entryClass.getDeclaredConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				throw new TableException(String.format("No default constructor in class '%s'", entryClass), e);
			}
		}

		this.entryConstructor = constructor;
	}

	protected Field[] resolveClassFields(Class<T> entryClass) {
		final var declaredFields = new ArrayList<Field>();
		for (final var field : entryClass.getDeclaredFields()) {
			final var mod = field.getModifiers();
			if (Modifier.isStatic(mod)) {
				continue;
			}

			declaredFields.add(field);
		}

		final var useAnnotations = declaredFields.stream().anyMatch(f -> f.isAnnotationPresent(TableField.class));
		if (!useAnnotations) {
			return declaredFields.toArray(Field[]::new);
		}

		final var filteredFields = new ArrayList<Field>();
		for (final var field : declaredFields) {
			final var annotation = field.getAnnotation(TableField.class);
			if (annotation == null) {
				continue;
			}
			filteredFields.add(field);
		}
		return filteredFields.toArray(Field[]::new);
	}

	protected void validateAssignability(Field[] fields, Column[] columns) {
		if (fields.length != columns.length) {
			throw new TableException(String.format("Number of fields (%d) in type '%s' does not match the number of expected columns (%d)", fields.length,
					this.entryClass, columns.length));
		}

		for (int i = 0; i < fields.length; ++i) {
			final var field = fields[i];
			final var actualType = field.getType();
			boolean isValid = false;

			switch (columns[i].type) {
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
				throw new ClassCastException(String.format("Column [#%d] is of type '%s' and can't be assigned to Field[#%d, %s] of type '%s' in class '%s'", i,
						columns[i].type, i, field.getName(), actualType, this.entryClass));
			}
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

	@SuppressWarnings("unchecked")
	protected Constructor<T> searchMatchingConstructor(Class<T> entryClass, Class<?>[] parameters) {
		final var constructors = entryClass.getDeclaredConstructors();

		for (final var constructor : constructors) {
			final var acceptedParameters = constructor.getParameters();
			if (acceptedParameters.length != parameters.length) {
				continue;
			}

			var isMatch = true;
			for (var i = 0; i < parameters.length && isMatch; ++i) {
				isMatch = canBeAssignedTo(parameters[i], acceptedParameters[i].getType());
			}

			if (isMatch) {
				return (Constructor<T>) constructor;
			}
		}

		return null;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public T[] buildEntries(Object[][] data) {
		final T[] entries = (T[]) Array.newInstance(this.entryClass, data.length);

		if (this.useConstructorInjection) {
			for (var i = 0; i < data.length; ++i) {
				try {
					entries[i] = this.entryConstructor.newInstance(data[i]);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new TableException(e);
				}
			}
		} else {
			final var accessModifier = new boolean[this.useableFields.length];
			for (var i = 0; i < this.useableFields.length; ++i) { // ignore private modifier
				accessModifier[i] = this.useableFields[i].isAccessible();
				this.useableFields[i].setAccessible(true);
			}
			try {
				for (var i = 0; i < data.length; ++i) {
					try {
						entries[i] = this.entryConstructor.newInstance();
						for (var j = 0; j < this.useableFields.length; ++j) {
							this.useableFields[j].set(entries[i], data[i][j]);
						}
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new TableException(e);
					}
				}
			} catch (final Exception e) {
				for (var i = 0; i < this.useableFields.length; ++i) {
					this.useableFields[i].setAccessible(accessModifier[i]);
				}
				throw e;
			}
		}

		return entries;
	}

	public T buildEntry(Object[] data) {
		return buildEntries(new Object[][] { data })[0];
	}
}
