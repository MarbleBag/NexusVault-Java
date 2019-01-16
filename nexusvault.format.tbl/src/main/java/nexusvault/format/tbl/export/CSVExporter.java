package nexusvault.format.tbl.export;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import nexusvault.format.tbl.RawTable;
import nexusvault.format.tbl.TableRecord;

/**
 * Simple exporter which is able to convert a {@link RawTable} to csv. The default element delimiter is <tt>;</tt> but can be changed. Each line will end with a
 * <tt>line feed</tt>.
 */
public final class CSVExporter {

	private final String elementDelimiter;

	public CSVExporter() {
		this(";");
	}

	public CSVExporter(String elementDelimiter) {
		this.elementDelimiter = elementDelimiter;
	}

	public void export(RawTable table, Writer out) throws IOException {
		final int fieldCount = table.getFieldCount();
		for (int i = 0; i < fieldCount; ++i) {
			if (i != 0) {
				out.append(elementDelimiter);
			}
			out.append(table.getFieldName(i));
		}
		out.append("\n");

		final List<TableRecord> records = table.getRecords();
		for (final TableRecord record : records) {
			for (int j = 0; j < fieldCount; ++j) {
				if (j != 0) {
					out.append(elementDelimiter);
				}
				final Object data = record.get(j);
				out.append(data != null ? data.toString() : "null");
			}
			out.append("\n");
		}
	}

}
