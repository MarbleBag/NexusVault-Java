package nexusvault.format.tbl;

import java.util.Collections;
import java.util.List;

public final class RawTable implements GameTable<TableRecord> {

	public final StructTableFileHeader header;
	public final StructTableFieldHeader[] fields;
	public final List<TableRecord> records;
	public final int[] lookUp;

	public RawTable(StructTableFileHeader header, StructTableFieldHeader[] fields, List<TableRecord> records, int[] lookUp) {
		this.header = header;
		this.fields = fields;
		this.records = records;
		this.lookUp = lookUp;
	}

	public String getTableName() {
		return this.header.name;
	}

	public int getFieldCount() {
		return this.fields.length;
	}

	public String getFieldName(int idx) {
		return this.fields[idx].getFieldName();
	}

	@Override
	public int size() {
		return records.size();
	}

	@Override
	public TableRecord getEntry(int recordIndex) {
		return records.get(recordIndex);
	}

	public List<TableRecord> getRecords() {
		return Collections.unmodifiableList(records);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Tbl [name=");
		builder.append(header);
		builder.append(", #fields=");
		if (fields != null) {
			builder.append(fields.length);
		} else {
			builder.append(0);
		}
		builder.append(", #data=");
		if (records != null) {
			builder.append(records.size());
		} else {
			builder.append(0);
		}
		builder.append("]");
		return builder.toString();
	}

}