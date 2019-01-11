package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructTextureFileHeader;

public class TextureObject {

	private final StructTextureFileHeader header;
	private final TextureRawData data;
	private final TextureDataDecoder interpreter;

	public TextureObject(StructTextureFileHeader header, TextureRawData data, TextureDataDecoder interpreter) {
		super();
		this.header = header;
		this.data = data;
		this.interpreter = interpreter;
	}

	public int getMipMapCount() {
		return header.mipMaps;
	}

	public byte[] getImageData(int idx) {
		if ((idx < 0) || (header.mipMaps < idx)) {
			throw new IndexOutOfBoundsException("Available index range [" + 0 + " and " + (header.mipMaps - 1) + ") was " + idx);
		}
		final int inverseIdx = header.mipMaps - 1 - idx;
		return this.interpreter.getImageData(header, data, inverseIdx);
	}

	public TextureImage getImage(int idx) {
		if ((idx < 0) || (header.mipMaps < idx)) {
			throw new IndexOutOfBoundsException("Available index range [" + 0 + " and " + (header.mipMaps - 1) + ") was " + idx);
		}
		final int inverseIdx = header.mipMaps - 1 - idx;
		return this.interpreter.getImage(header, data, inverseIdx);
	}

}
