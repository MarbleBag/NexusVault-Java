package nexusvault.format.tex;

import java.nio.ByteOrder;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteArrayBinaryReader;
import nexusvault.format.tex.dxt.DXTImageReader;
import nexusvault.format.tex.jpg.JPGImageReader;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.format.tex.uncompressed.PlainImageReader;

/**
 * @see DXTImageReader
 * @see JPGImageReader
 * @see PlainImageReader
 * @see Texture
 */
public final class TextureReader {
	private TextureReader() {

	}

	public static StructFileHeader getFileHeader(byte[] data) {
		return readFileHeader(new ByteArrayBinaryReader(data, ByteOrder.LITTLE_ENDIAN));
	}

	public static StructFileHeader readFileHeader(BinaryReader reader) {
		return new StructFileHeader(reader);
	}

	public static Texture read(byte[] data) {
		return new Texture(data);
	}

	public static Image readFirstImage(byte[] data) {
		final var texture = read(data);
		return texture.getMipMap(0);
	}

}
