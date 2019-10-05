package nexusvault.format.tex.unc;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImageFormat;

final class Gray8Encoder implements UncompressedEncoder {

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
		final int sourceLength = width * height;
		for (int i = 0; i < sourceLength; ++i) {
			final var a = source.readUInt8();
			final var r = source.readUInt8();
			final var g = source.readUInt8();
			final var b = source.readUInt8();
			final var luminosity = Math.min(255, Math.max(0, Math.round((0.21f * r) + (0.72f * g) + (0.07f * b))));
			destination.writeInt8(luminosity);
		}
	}

	private void encodeRGB(BinaryReader source, int width, int height, BinaryWriter destination) {
		final int sourceLength = width * height;
		for (int i = 0; i < sourceLength; ++i) {
			final var r = source.readUInt8();
			final var g = source.readUInt8();
			final var b = source.readUInt8();
			final var luminosity = Math.min(255, Math.max(0, Math.round((0.21f * r) + (0.72f * g) + (0.07f * b))));
			destination.writeInt8(luminosity);
		}
	}

	public void encodeGray(BinaryReader source, int width, int height, BinaryWriter destination) {
		final int length = TextureImageFormat.GRAYSCALE.getBytesPerPixel() * width * height;
		final int written = destination.write(source, length);
		if (written != length) {
			throw new TextureConversionException(/* TODO */);
		}
	}

}
