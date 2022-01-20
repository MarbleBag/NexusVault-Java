package nexusvault.format.tbl;

import nexusvault.format.tbl.struct.StructColumn;

public enum ColumnType {
	UNK(0),
	FLOAT(4),
	INT32(3),
	BOOL(11),
	INT64(20),
	STRING(130);

	public final int value;

	private ColumnType(int flag) {
		this.value = flag;
	}

	public static ColumnType resolve(StructColumn column) {
		return resolve(column.type);
	}

	public static ColumnType resolve(int type) {
		for (final ColumnType t : ColumnType.values()) {
			if (t.value == type) {
				return t;
			}
		}
		return UNK;
	}
}