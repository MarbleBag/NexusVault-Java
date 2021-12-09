package nexusvault.format.tex.unc;

import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.util.ColorModelConverter;

final class Gray8ImageEncoder implements UncompressedImageEncoder {

	@Override
	public byte[] encode(byte[] source, int width, int height, TextureImageFormat format) {
		final var expectedSize = TextureImageFormat.GRAYSCALE.getBytesPerPixel() * width * height;

		byte[] encoded = null;
		switch (format) {
			case ARGB:
				encodeARGB(source, width, height);
				encoded = ColorModelConverter.convertARGBToGrayscale(source);
				break;
			case RGB:
				encoded = encodeRGB(source, width, height);
				break;
			case GRAYSCALE:

				encoded = encodeGray(source, width, height);
				break;
			default:
				throw new TextureConversionException(/* TODO */);
		}

		if (encoded.length != expectedSize) {

		}
		return encoded;
	}

	/**
	 * Converts ARGB to Grayscale
	 */
	private byte[] encodeARGB(byte[] source, int width, int height) {
		final byte[] imageData = new byte[TextureImageFormat.GRAYSCALE.getBytesPerPixel() * width * height];
		for (int s = 0, d = 0; d < imageData.length; s += 4, d += 1) {
			final var a = source[s + 0]; // not used in grayscale
			final var r = source[s + 1];
			final var g = source[s + 2];
			final var b = source[s + 3];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			imageData[d] = (byte) luminosity;
		}
		return imageData;
	}

	/**
	 * Converts RGB to Grayscale
	 */
	private byte[] encodeRGB(byte[] source, int width, int height) {
		final byte[] imageData = new byte[TextureImageFormat.GRAYSCALE.getBytesPerPixel() * width * height];
		for (int s = 0, d = 0; d < imageData.length; s += 3, d += 1) {
			final var r = source[s + 0];
			final var g = source[s + 1];
			final var b = source[s + 2];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			imageData[d] = (byte) luminosity;
		}
		return imageData;
	}

	/**
	 * Converts Grayscale to Grayscale (It just makes a copy)
	 */
	private byte[] encodeGray(byte[] source, int width, int height) {
		final byte[] imageData = new byte[TextureImageFormat.GRAYSCALE.getBytesPerPixel() * width * height];
		System.arraycopy(source, 0, imageData, 0, imageData.length);
		return imageData;
	}

}
