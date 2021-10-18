package nexusvault.format.tbl;

import java.util.Collections;
import java.util.List;

import nexusvault.format.tbl.struct.StructColumnData;
import nexusvault.format.tbl.struct.StructTableFileHeader;

/**
 * Table which contains all information on the read .tbl file. Its entries are stored in untyped {@link TableRecord TableRecords}. To increase its usability,
 * instances of this class should be wrapped in a typed {@link GameTable} like {@link LazyLoadedTypedTable} or {@link PreloadedTypedTable}
 */
public final class RawTable implements GameTable<TableRecord> {

	public final StructTableFileHeader header;
	public final StructColumnData[] fields;
	public final List<TableRecord> records;
	public final int[] lookUp;

	public RawTable(StructTableFileHeader header, StructColumnData[] fields, List<TableRecord> records, int[] lookUp) {
		this.header = header;
		this.fields = fields;
		this.records = records;
		this.lookUp = lookUp;
	}

	public String getTableName() {
		return null; // this.header.name;
	}

	public int getFieldCount() {
		return this.fields.length;
	}

	public String getFieldName(int idx) {
		return null; // this.fields[idx].getFieldName();
	}

	@Override
	public int size() {
		return this.records.size();
	}

	@Override
	public TableRecord getEntry(int recordIndex) {
		return this.records.get(recordIndex);
	}

	public List<TableRecord> getRecords() {
		return Collections.unmodifiableList(this.records);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Tbl [name=");
		builder.append(this.header);
		builder.append(", #fields=");
		if (this.fields != null) {
			builder.append(this.fields.length);
		} else {
			builder.append(0);
		}
		builder.append(", #data=");
		if (this.records != null) {
			builder.append(this.records.size());
		} else {
			builder.append(0);
		}
		builder.append("]");
		return builder.toString();
	}

}