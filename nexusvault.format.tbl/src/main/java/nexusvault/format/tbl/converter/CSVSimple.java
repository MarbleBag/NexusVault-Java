package nexusvault.format.tbl.converter;

import java.io.IOException;
import java.io.Writer;

import nexusvault.format.tbl.Table;

/**
 * Simple exporter which is able to convert a {@link Table} to csv. The default element delimiter is <code>;</code> but can be changed. Each line will end with
 * a <code>line feed</code>.
 */
public final class CSVSimple {

	private final String elementDelimiter;

	public CSVSimple() {
		this(";");
	}

	public CSVSimple(String elementDelimiter) {
		this.elementDelimiter = elementDelimiter;
	}

	public void write(Table table, Writer out) throws IOException {
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
