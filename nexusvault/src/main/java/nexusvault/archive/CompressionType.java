package nexusvault.archive;

public enum CompressionType {
	// PREVIOUS(-1),
	NONE(0),
	ZIP(2 | 1),
	LZMA(4 | 1);

	private final int flag;

	private CompressionType(int flag) {
		this.flag = flag;
	}

	public int getFlag() {
		return flag;
	}
}