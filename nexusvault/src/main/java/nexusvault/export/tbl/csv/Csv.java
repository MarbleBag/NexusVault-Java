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

package nexusvault.export.tbl.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import nexusvault.format.tbl.Column;
import nexusvault.format.tbl.ColumnType;
import nexusvault.format.tbl.Table;
import nexusvault.format.tbl.TableException;
import nexusvault.format.tbl.TableLookup;

/**
 * Exporter which is able to convert a {@link Table} to csv.
 */
public final class Csv {

	private final String elementDelimiter;

	public Csv() {
		this(";");
	}

	public Csv(String elementDelimiter) {
		this.elementDelimiter = elementDelimiter;
	}

	/**
	 * Writes a complex csv representation of a {@link Table}.
	 * <ul>
	 * <li>The first row will contain the table name as stored in the file.</li>
	 * <li>The second row will contain the table columns, a combination of its name, datatype and a still unknown value</li>
	 * <li>Then, each entry will be written to one row.</li>
	 * </ul>
	 * The default element delimiter is <code>;</code> but can be changed. Each line will end with a <code>line feed</code>.
	 */
	public void write(Table table, Writer out) throws IOException {
		out.append(table.name).append("\n");
		for (int i = 0; i < table.columns.length; ++i) {
			if (i != 0) {
				out.append(this.elementDelimiter);
			}
			out.append(table.columns[i].name);
			out.append(" [");
			out.append(table.columns[i].type.toString());
			out.append(" ").append(Long.toString(table.columns[i].unk2));
			out.append("]");
		}
		out.append("\n");
		for (final var entry : table.entries) {
			for (int j = 0; j < table.columns.length; ++j) {
				if (j != 0) {
					out.append(this.elementDelimiter);
				}
				final Object data = entry[j];
				out.append(data != null ? data.toString() : "null");
			}
			out.append("\n");
		}
	}

	public Table read(Reader in) throws IOException {
		final var reader = new BufferedReader(in);

		final String tableName = reader.readLine();

		Column[] columns = null;
		{
			final var elements = reader.readLine().split(this.elementDelimiter, -1);
			columns = new Column[elements.length];
			for (var i = 0; i < columns.length; ++i) {
				var parts = elements[i].split(" \\[");
				final var name = parts[0].strip();
				parts[1] = parts[1].substring(0, parts[1].length() - 1);
				parts = parts[1].split(" ");
				final var datatype = ColumnType.valueOf(parts[0]);
				final var unk2 = Long.parseLong(parts[1]);
				columns[i] = new Column(name, datatype, unk2);
			}
		}

		Object[][] entries = null;
		{
			final var list = new ArrayList<Object[]>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				final var entry = new Object[columns.length];
				list.add(entry);

				final var parts = line.split(this.elementDelimiter, columns.length);
				for (var i = 0; i < columns.length; ++i) {
					switch (columns[i].type) {
						case BOOL:
							entry[i] = Boolean.parseBoolean(parts[i]);
							break;
						case INT32:
							entry[i] = Integer.parseInt(parts[i]);
							break;
						case INT64:
							entry[i] = Long.parseLong(parts[i]);
							break;
						case FLOAT:
							entry[i] = Float.parseFloat(parts[i]);
							break;
						case STRING:
							entry[i] = parts[i];
							break;
						default:
							throw new TableException(String.format("Unknow data type: '%s'", columns[i].type));
					}
				}
			}
			entries = list.toArray(Object[][]::new);
		}

		final var lookup = TableLookup.sortEntriesAndComputeLookup(entries);
		return new Table(tableName, columns, entries, lookup);
	}

	/**
	 * Writes a simple csv representation of a {@link Table}.
	 * <ul>
	 * <li>The first row will contain the names of the columns, one name per column.</li>
	 * <li>Then, each entry will be written to one row.</li>
	 * </ul>
	 * This simplified version will not be accepted by {@link #read(Reader)}.
	 *
	 * The default element delimiter is <code>;</code> but can be changed. Each line will end with a <code>line feed</code>.
	 */
	public void writeSimple(Table table, Writer out) throws IOException {
		for (int i = 0; i < table.columns.length; ++i) {
			if (i != 0) {
				out.append(this.elementDelimiter);
			}
			out.append(table.columns[i].name);
		}
		out.append("\n");
		for (final var entry : table.entries) {
			for (int j = 0; j < table.columns.length; ++j) {
				if (j != 0) {
					out.append(this.elementDelimiter);
				}
				final Object data = entry[j];
				out.append(data != null ? data.toString() : "null");
			}
			out.append("\n");
		}
	}

}
