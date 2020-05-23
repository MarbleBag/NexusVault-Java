package nexusvault.format.m3.export.gltf.internal;

public enum GlTFComponentType {
	INT8(5120, 1),
	UINT8(5121, 1),
	INT16(5122, 2),
	UINT16(5123, 2),
	UINT32(5125, 4),
	FLOAT(5126, 4);

	private final int id;
	private final int byteCount;

	private GlTFComponentType(int id, int byteCount) {
		this.id = id;
		this.byteCount = byteCount;
	}

	public int getId() {
		return this.id;
	}

	public int getByteCount() {
		return this.byteCount;
	}
}