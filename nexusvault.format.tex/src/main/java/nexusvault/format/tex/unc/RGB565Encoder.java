package nexusvault.format.tex.unc;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImageFormat;

final class RGB565Encoder implements UncompressedEncoder {

	@Override
	public boolean accepts(TextureImageFormat format) {
		switch (format) {
			case ARGB:
			case RGB:
			case GRAYSCALE:
				return true;
			default:
				return false;
		}
	}

	@Override
	public void encode(BinaryReader source, TextureImageFormat format, int width, int height, BinaryWriter destination) {
		switch (format) {
			case ARGB:
				encodeARGB(source, width, height, destination);
				break;
			case RGB:
				encodeRGB(source, width, height, destination);
				break;
			case GRAYSCALE:
				encodeGray(source, width, height, destination);
				break;
			default:
				throw new TextureConversionException(/* TODO */);
		}
	}

	private void encodeARGB(BinaryReader source, int width, int height, BinaryWriter destination) {
		final int numberOfPixels = width * height;
		for (int i = 0; i < numberOfPixels; ++i) {
			final var a = source.readInt8();
			final var r = source.readInt8();
			final var g = source.readInt8();
			final var b = source.readInt8();
			destination.writeInt8(r);
			destination.writeInt8(g);
			destination.writeInt8(b);
		}
	}

	public void encodeRGB(BinaryReader source, int width, int height, BinaryWriter destination) {
		final int numberOfPixels = width * height;
		for (int i = 0; i < numberOfPixels; ++i) {
			final short data = (short) (((source.readInt8() & 0x1F) << 11) | ((source.readInt8() & 0x3F) << 5) | ((source.readInt8() & 0x1F) << 0));
			destination.writeInt16(data);
		}
	}

	private void encodeGray(BinaryReader source, int width, int height, BinaryWriter destination) {
		final int numberOfPixels = width * height;
		for (int i = 0; i < numberOfPixels; ++i) {
			final var gray = source.readInt8();
			destination.writeInt8(gray);
			destination.writeInt8(gray);
			destination.writeInt8(gray);
		}
	}

}
