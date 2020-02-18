package nexusvault.format.tex.unc;

import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImageFormat;

final class RGB565ImageEncoder implements UncompressedImageEncoder {

	@Override
	public byte[] encode(byte[] source, int width, int height, TextureImageFormat format) {
		switch (format) {
			case ARGB:
				return encodeARGB(source, width, height);
			case RGB:
				return encodeRGB(source, width, height);
			case GRAYSCALE:
				return encodeGray(source, width, height);
			default:
				throw new TextureConversionException(/* TODO */);
		}
	}

	// something is still fishy here

	/**
	 * Converts ARGB to BGR565
	 */
	private byte[] encodeARGB(byte[] source, int width, int height) {
		final byte[] imageData = new byte[2 /* two bytes per pixel */ * width * height];
		for (int s = 0, d = 0; d < imageData.length; s += 4, d += 2) {
			// var a = source[s+0]; //A //not used
			final var r = source[s + 1]; // R
			final var g = source[s + 2]; // G
			final var b = source[s + 3]; // B

			final int data = (r & 0xF8) << 8 | (g & 0xFC) << 3 | (b & 0xF8) << 0;

			imageData[d + 1] = (byte) (data >> 8 & 0xFF);
			imageData[d + 0] = (byte) (data >> 0 & 0xFF);
		}
		return imageData;
	}

	/**
	 * Converts RGB to BGR565
	 */
	private byte[] encodeRGB(byte[] source, int width, int height) {
		final byte[] imageData = new byte[2 /* two bytes per pixel */ * width * height];
		for (int s = 0, d = 0; d < imageData.length; s += 3, d += 2) {
			final var r = source[s + 0]; // R
			final var g = source[s + 1]; // G
			final var b = source[s + 2]; // B

			final int data = (r & 0xF8) << 8 | (g & 0xFC) << 3 | (b & 0xF8) << 0;

			imageData[d + 1] = (byte) (data >> 8 & 0xFF);
			imageData[d + 0] = (byte) (data >> 0 & 0xFF);
		}
		return imageData;
	}

	/**
	 * Converts Grayscale to BGR565
	 */
	private byte[] encodeGray(byte[] source, int width, int height) {
		final byte[] imageData = new byte[2 /* two bytes per pixel */ * width * height];
		for (int s = 0, d = 0; d < imageData.length; s += 1, d += 2) {
			final var shade = source[s + 0]; // R

			final int data = (shade & 0xF8) << 8 | (shade & 0xFC) << 3 | (shade & 0xF8) << 0;

			imageData[d + 1] = (byte) (data >> 8 & 0xFF);
			imageData[d + 0] = (byte) (data >> 0 & 0xFF);
		}
		return imageData;
	}

}
