package nexusvault.format.tex;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.struct.StructTextureFileHeader;

final class DecodeableTextureObject implements TextureObject {

	private final StructTextureFileHeader header;
	private final BinaryReader data;
	private final TextureImageReader reader;

	public DecodeableTextureObject(StructTextureFileHeader header, BinaryReader data, TextureImageReader reader) {
		this.header = header;
		this.data = data;
		this.reader = reader;
	}

	@Override
	public int getMipMapCount() {
		return header.mipMaps;
	}

	@Override
	public int getImageWidth() {
		return header.width;
	}

	@Override
	public int getImageHeight() {
		return header.height;
	}

	@Override
	public TexType getTextureDataType() {
		return TexType.resolve(header);
	}

	@Override
	public TextureImageFormat getTextureImageFormat() {
		return reader.getImageFormat();
	}

	@Override
	public byte[] getImageData(int idx) {
		return reader.getUnprocessedImageData(header, data, computeMipIndex(idx));
	}

	@Override
	public TextureImage getImage(int idx) {
		return reader.read(header, data, computeMipIndex(idx));
	}

	private int computeMipIndex(int idx) {
		return header.mipMaps - 1 - idx;
	}

}
