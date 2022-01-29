package nexusvault.format.tex;

import nexusvault.format.tex.Image.ImageFormat;
import nexusvault.format.tex.dxt.DXTImageReader;
import nexusvault.format.tex.jpg.JPGImageReader;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.format.tex.uncompressed.PlainImageReader;

public final class Texture {

	public static Texture read(byte[] data) {
		return new Texture(data);
	}

	private final StructFileHeader header;
	private final byte[] data;

	public Texture(byte[] data) {
		this.header = TextureReader.getFileHeader(data);
		this.data = data;
	}

	public TextureType getTextureType() {
		return TextureType.resolve(this.header);
	}

	public Image.ImageFormat getImageFormat() {
		switch (getTextureType()) {
			case ARGB1:
			case ARGB2:
			case DXT1:
			case DXT3:
			case DXT5:
			case JPG1:
			case JPG2:
			case JPG3:
				return ImageFormat.ARGB;
			case RGB:
				return ImageFormat.RGB;
			case GRAYSCALE:
				return ImageFormat.GRAYSCALE;
			default:
				throw new TextureException();
		}
	}

	public Image getMipMap(int index) {
		index = this.header.mipMaps - index - 1;
		switch (getTextureType()) {
			case DXT1:
			case DXT3:
			case DXT5:
				return DXTImageReader.decompress(this.header, this.data, StructFileHeader.SIZE_IN_BYTES, index);
			case JPG1:
			case JPG2:
			case JPG3:
				return JPGImageReader.decompress(this.header, this.data, StructFileHeader.SIZE_IN_BYTES, index);
			case ARGB1:
			case ARGB2:
			case RGB:
			case GRAYSCALE:
				return PlainImageReader.getImage(this.header, this.data, StructFileHeader.SIZE_IN_BYTES, index);
			default:
				throw new TextureException();
		}
	}

	public int getMipMapCount() {
		return this.header.mipMaps;
	}

	public int getWidth() {
		return this.header.width;
	}

	public int getHeight() {
		return this.header.width;
	}

	public int getVersion() {
		return this.header.version;
	}

	public int getSides() {
		return this.header.sides;
	}

	public int getDepth() {
		return this.header.depth;
	}

}
