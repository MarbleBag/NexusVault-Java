package nexusvault.format.tbl.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import nexusvault.format.tbl.Column;
import nexusvault.format.tbl.Table;
import nexusvault.format.tbl.TableEntriesAndLookup;
import nexusvault.format.tbl.TableException;
import nexusvault.format.tbl.struct.DataType;

/**
 * Simple exporter which is able to convert a {@link Table} to csv. The default element delimiter is <code>;</code> but can be changed. Each line will end with
 * a <code>line feed</code>.
 */
public final class CSV {

	private final String elementDelimiter;

	public CSV() {
		this(";");
	}

	public CSV(String elementDelimiter) {
		this.elementDelimiter = elementDelimiter;
	}

	public void write(Table table, Writer out) throws IOException {
		out.append(table.name).append(this.elementDelimiter).append(Long.toString(table.unk1)).append("\n");
		for (int i = 0; i < table.columns.length; ++i) {
			if (i != 0) {
				out.append(this.elementDelimiter);
			}
			out.append(table.columns[i].name);
			out.append(" [");
			out.append(table.columns[i].dataType.toString());
			out.append(" ").append(Long.toString(table.columns[i].unk1));
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

		String tableName = null;
		long tableUnk1 = 0;
		{
			final var elements = reader.readLine().split(this.elementDelimiter, -1);
			tableName = elements[0];
			tableUnk1 = Long.parseLong(elements[1]);
		}

		Column[] columns = null;
		{
			final var elements = reader.readLine().split(this.elementDelimiter, -1);
			columns = new Column[elements.length];
			for (var i = 0; i < columns.length; ++i) {
				var parts = elements[i].split(" \\[");
				final var name = parts[0].strip();
				parts[1] = parts[1].substring(0, parts[1].length() - 1);
				parts = parts[1].split(" ");
				final var datatype = DataType.valueOf(parts[0]);
				final var unk1 = Long.parseLong(parts[1]);
				final var unk2 = Long.parseLong(parts[2]);
				columns[i] = new Column(name, datatype, unk1, unk2);
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
					switch (columns[i].dataType) {
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
							throw new TableException(String.format("Unknow data type: '%s'", columns[i].dataType));
					}
				}
			}
			entries = list.toArray(Object[][]::new);
		}

		final var lookup = TableEntriesAndLookup.sortAndComputeLookup(entries);
		return new Table(tableName, columns, entries, lookup, tableUnk1);
	}

}
