package nexusvault.format.m3;

public enum VertexField {
	/** 3 x float32 values. Location of vertex in decimal space as xyz */
	LOCATION_A(0, 1, 3, DataFormat.FLOAT32),

	/** 3 x in16 values. Location of vertex in integer space as xyz */
	LOCATION_B(0, 2, 3, DataFormat.INT16),

	/** 2 byte */
	FIELD_3_UNK_1(1, -1, 2, DataFormat.UNKNOWN), // prob. -tangents- / normals / binormals

	/** 2 byte */
	FIELD_3_UNK_2(2, -1, 2, DataFormat.UNKNOWN), // prob. tangents / -normals- / binormals

	/** 2 byte */
	FIELD_3_UNK_3(3, -1, 2, DataFormat.UNKNOWN), // prob. tangents / normals / -binormals-

	/** 4 x int8 values. Each value represents a bone index. A value of 0, besides the first value, indicates the value is not used. */
	BONE_MAP(4, 4, 4, DataFormat.UINT8),

	/** 4 x int8 values. A value of 0 indicates the value is not used. All values sum up to 255 */
	BONE_WEIGHTS(5, 4, 4, DataFormat.UINT8),

	/** 4 byte */
	FIELD_4_UNK_1(6, -1, 4, DataFormat.UNKNOWN),

	/** 4 byte */
	FIELD_4_UNK_2(7, -1, 4, DataFormat.UNKNOWN),

	/** 2 x float16 values, uv */
	UV_MAP_1(8, 5, 2, DataFormat.FLOAT16),

	/** 2 x float16 values, uv */
	UV_MAP_2(9, 5, 2, DataFormat.FLOAT16),

	/** 1 byte */
	FIELD_6_UNK_1(10, -1, 1, DataFormat.UNKNOWN);

	final int index;
	final int type;

	private final int elements;
	private final DataFormat format;

	private VertexField(int index, int type, int elements, DataFormat format) {
		this.index = index;
		this.type = type;

		this.elements = elements;
		this.format = format;
	}

	public int getIndex() {
		return this.index;
	}

	public int getType() {
		return this.type;
	}

	public int getNumberOfElements() {
		return this.elements;
	}

	public DataFormat getFormat() {
		return this.format;
	}

	public int getSizeInBytes() {
		return this.elements * this.format.getSizeInBytes();
	}
}