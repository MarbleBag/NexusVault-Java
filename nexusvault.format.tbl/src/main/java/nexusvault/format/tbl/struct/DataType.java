package nexusvault.format.tbl.struct;

public enum DataType {
	UNK(0),
	FLOAT(4),
	INT32(3),
	BOOL(11),
	INT64(20),
	STRING(130);

	public final int value;

	private DataType(int flag) {
		this.value = flag;
	}

	public static DataType resolve(int flag) {
		for (final DataType t : DataType.values()) {
			if (t.value == flag) {
				return t;
			}
		}
		return UNK;
	}
}