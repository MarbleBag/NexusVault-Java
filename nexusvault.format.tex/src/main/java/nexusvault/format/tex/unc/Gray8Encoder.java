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
		// TODO Auto-generated method stub

	}

	private void encodeRGB(BinaryReader source, int width, int height, BinaryWriter destination) {
		// TODO Auto-generated method stub

	}

	public void encodeGray(BinaryReader source, int width, int height, BinaryWriter destination) {
		final int length = TextureImageFormat.GRAYSCALE.getBytesPerPixel() * width * height;
		final int written = destination.write(source, length);
		if (written != length) {
			throw new TextureConversionException(/* TODO */);
		}
	}

}
