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
	public int getVersion() {
		return this.header.version;
	}

	@Override
	public int getDepth() {
		return this.header.depth;
	}

	@Override
	public int getSides() {
		return this.header.sides;
	}

	@Override
	public int getMipMapCount() {
		return this.header.mipMaps;
	}

	@Override
	public int getImageWidth() {
		return this.header.width;
	}

	@Override
	public int getImageHeight() {
		return this.header.height;
	}

	@Override
	public TexType getTextureDataType() {
		return TexType.resolve(this.header);
	}

	@Override
	public TextureImageFormat getTextureImageFormat() {
		return this.reader.getImageFormat();
	}

	@Override
	public byte[] getImageData(int idx) {
		return this.reader.getUnprocessedImageData(this.header, this.data, computeMipIndex(idx));
	}

	@Override
	public TextureImage getImage(int idx) {
		return this.reader.read(this.header, this.data, computeMipIndex(idx));
	}

	private int computeMipIndex(int idx) {
		return this.header.mipMaps - 1 - idx;
	}

}
