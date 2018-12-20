package nexusvault.format.tbl;

enum FieldDataType {
	UNK(0),
	FLOAT(4),
	INT32(3),
	BOOL(11),
	INT64(20),
	STRING(130),
	UNK_STR(128);
	private int flag;

	private FieldDataType(int flag) {
		this.flag = flag;
	}

	public static FieldDataType resolve(int flag) {
		for (final FieldDataType t : FieldDataType.values()) {
			if (t.flag == flag) {
				return t;
			}
		}
		return UNK;
	}
}