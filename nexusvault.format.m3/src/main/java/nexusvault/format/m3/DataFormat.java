package nexusvault.format.m3;

public enum DataFormat {
	UNKNOWN(1),
	UINT8(1),
	UINT16(2),
	INT16(2),
	INT32(4),
	FLOAT16(2),
	FLOAT32(4);

	private int sizeInBytes;

	private DataFormat(int sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}

	public int getSizeInBytes() {
		return this.sizeInBytes;
	}
}