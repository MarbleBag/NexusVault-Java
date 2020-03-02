package nexusvault.format.tex.unc;

import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImageFormat;

final class ARGB8888ImageEncoder implements UncompressedImageEncoder {

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

	/**
	 * Converts ARGB to BGRA8888
	 */
	private byte[] encodeARGB(byte[] source, int width, int height) {
		final byte[] imageData = new byte[TextureImageFormat.ARGB.getBytesPerPixel() * width * height];
		for (int s = 0; s < imageData.length; s += 4) {
			imageData[s + 0] = source[s + 3]; // B;
			imageData[s + 1] = source[s + 2]; // G;
			imageData[s + 2] = source[s + 1]; // R;
			imageData[s + 3] = source[s + 0]; // A;
		}
		return imageData;
	}

	/**
	 * Converts RGB to BGRA8888
	 */
	private byte[] encodeRGB(byte[] source, int width, int height) {
		final byte[] imageData = new byte[TextureImageFormat.ARGB.getBytesPerPixel() * width * height];
		for (int s = 0, d = 0; d < imageData.length; s += 3, d += 4) {
			imageData[d + 0] = source[s + 2]; // B;
			imageData[d + 1] = source[s + 1]; // G;
			imageData[d + 2] = source[s + 0]; // R;
			imageData[d + 3] = (byte) 0xFF;// A
		}
		return imageData;
	}

	/**
	 * Converts Grayscale to BGRA8888
	 */
	private byte[] encodeGray(byte[] source, int width, int height) {
		final byte[] imageData = new byte[TextureImageFormat.ARGB.getBytesPerPixel() * width * height];
		for (int s = 0, d = 0; d < imageData.length; s += 1, d += 4) {
			imageData[d + 0] = source[s + 0]; // shade;
			imageData[d + 1] = source[s + 0]; // shade;
			imageData[d + 2] = source[s + 0]; // shade;
			imageData[d + 3] = (byte) 0xFF;// A
		}
		return imageData;
	}

}
