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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kreed.reflection.struct.FieldExtractor;
import kreed.reflection.struct.StructField;
import nexusvault.format.m3.debug.Table.DataType;
import nexusvault.format.m3.debug.Table.TableCell;
import nexusvault.format.m3.debug.Table.TableColumn;
import nexusvault.format.m3.debug.Table.TableRow;
import nexusvault.format.m3.pointer.ArrayTypePointer;
import nexusvault.format.m3.pointer.DoubleArrayTypePointer;

public final class BasicStructFormater implements StructFormater {

	private final Class2ObjectLookup<FieldFormater> fieldFormaters;
	private final Map<String, FieldFormater> fieldFormaterByName;

	public BasicStructFormater() {
		this.fieldFormaters = new Class2ObjectLookup<>(null);
		this.fieldFormaterByName = new HashMap<>();

		this.fieldFormaters.setLookUp(ArrayTypePointer.class, new ATPFieldFormater());
		this.fieldFormaters.setLookUp(DoubleArrayTypePointer.class, new DATPFieldFormater());

		final var baseTypes = new ArrayList<Class<?>>();
		baseTypes.add(Object.class);
		baseTypes.add(byte.class);
		baseTypes.add(short.class);
		baseTypes.add(int.class);
		baseTypes.add(long.class);
		baseTypes.add(float.class);
		baseTypes.add(double.class);

		final var untypedFieldFormater = new UntypedFieldFormater();
		for (final var c : baseTypes) {
			this.fieldFormaters.setLookUp(c, untypedFieldFormater);
		}
	}

	public void setFieldFormater(Class<?> fieldType, FieldFormater formater) {
		this.fieldFormaters.setLookUp(fieldType, formater);
	}

	public void setFieldFormater(String fieldName, FieldFormater formater) {
		this.fieldFormaterByName.put(fieldName, formater);
	}

	@Override
	public Table formatTable(DebugInfo debuger, long dataOffset, Class<?> structClass, int structCount) {
		final List<Object> structs = debuger.loadStructs(dataOffset, structClass, structCount);
		final List<Field> fields = loadFields(structClass);

		final Table table = createInitialTable(fields);
		final List<FieldFormater> fieldFormaters = getFieldFormater(fields);

		for (final Object struct : structs) {
			final TableRow nextRow = table.addNewRow();
			for (int fieldNo = 0; fieldNo < fields.size(); ++fieldNo) {
				final Field field = fields.get(fieldNo);
				final TableColumn column = table.getColumn(field.getName());
				final TableCell cell = column.getCell(nextRow);
				final FieldFormater fieldFormater = fieldFormaters.get(fieldNo);
				final FieldReader fieldReader = createFieldReader(struct, field);

				fieldFormater.processField(debuger, cell, fieldReader);
			}
		}

		return table;
	}

	private List<FieldFormater> getFieldFormater(List<Field> fields) {
		final List<FieldFormater> formater = new ArrayList<>(fields.size());
		for (final Field field : fields) {
			formater.add(getFieldFormater(field));
		}
		return formater;
	}

	private FieldFormater getFieldFormater(Field field) {
		FieldFormater formater = this.fieldFormaterByName.get(field.getName());
		if (formater != null) {
			return formater;
		}

		final Class<?> fieldType = field.getType().isArray() ? field.getType().getComponentType() : field.getType();
		formater = this.fieldFormaters.getLookUp(fieldType);
		if (formater == null) {
			// if (fieldType.isPrimitive()) {
			// formater = new UntypedFieldFormater();
			// fieldFormaters.setLookUp(fieldType, formater);
			// } else {
			throw new IllegalStateException("W00T");
			// }
		}
		return formater;
	}

	private Table createInitialTable(List<Field> fields) {
		return new Table(createTableColumns(fields));
	}

	private List<TableColumn> createTableColumns(List<Field> fields) {
		final var headers = new LinkedList<TableColumn>();
		// headers.add(new TableColumn("Model"));
		// headers.add(new TableColumn("Struct Name"));
		for (final var field : fields) {
			final var fieldAnnotation = field.getAnnotation(StructField.class);
			final var header = new TableColumn(field.getName(), field.getName(), DataType.resolveType(field.getType(), fieldAnnotation.value()));
			if (field.getType().isArray()) {
				final int size = fieldAnnotation.length();
				header.setArrayAnnotation(size);
			}
			headers.add(header);
		}
		return headers;
	}

	private List<Field> loadFields(Class<?> structClass) {
		final List<Field> fields = FieldExtractor.getStructFields(structClass, false);
		for (final var field : fields) {
			field.setAccessible(true);
		}
		return fields;
	}

	private FieldReader createFieldReader(Object struct, Field structField) {
		final boolean isArray = structField.getType().isArray();
		try {
			if (isArray) {
				return new ArrayFieldReader(struct, structField);
			} else {
				return new BufferedFieldReader(struct, structField);
			}
		} catch (final IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

}