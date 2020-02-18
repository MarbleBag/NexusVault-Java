package nexusvault.format.tex;

public final class TextureImageTypeConverter {

	public static TextureImage convertToType(TextureImage source, TextureImageFormat target) {
		if (source.getImageFormat() == target) {
			return source;
		}
		final var data = convert(source, target);
		return new TextureImage(source.getImageWidth(), source.getImageHeight(), target, data);
	}

	private static byte[] convert(TextureImage source, TextureImageFormat target) {
		final var image = source.getImageData();
		switch (source.getImageFormat()) {
			case ARGB:
				switch (target) {
					case GRAYSCALE:
						return convertARGBToGrayscale(image);
					case RGB:
						return convertARGBToRGB(image);
				}
			case GRAYSCALE:
				switch (target) {
					case ARGB:
						return convertGrayscaleToARGB(image);
					case RGB:
						return convertGrayscaleToRGB(image);
				}
			case RGB:
				switch (target) {
					case ARGB:
						return convertRGBToARGB(image);
					case GRAYSCALE:
						return convertRGBToGrayscale(image);
				}
			default:
				throw new IllegalArgumentException(String.format("Unsupported source[%s] or target format[%s]", source.getImageFormat(), target));
		}
	}

	private static byte[] convertARGBToGrayscale(byte[] src) {
		final byte[] dst = new byte[src.length / 4];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 1) {
			final var a = src[s + 0]; // not used in grayscale
			final var r = src[s + 1];
			final var g = src[s + 2];
			final var b = src[s + 3];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			dst[d] = (byte) luminosity;
		}
		return dst;
	}

	private static byte[] convertARGBToRGB(byte[] src) {
		final byte[] dst = new byte[src.length / 4 * 3];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 3) {
			dst[d + 0] = src[s + 1];
			dst[d + 1] = src[s + 2];
			dst[d + 2] = src[s + 3];
		}
		return dst;
	}

	private static byte[] convertGrayscaleToARGB(byte[] src) {
		final byte[] dst = new byte[src.length * 4];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 4) {
			dst[d + 0] = (byte) 0xFF;
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
			dst[d + 3] = src[s + 0];
		}
		return dst;
	}

	private static byte[] convertGrayscaleToRGB(byte[] src) {
		final byte[] dst = new byte[src.length * 3];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 3) {
			dst[d + 0] = src[s + 0];
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
		}
		return dst;
	}

	private static byte[] convertRGBToARGB(byte[] src) {
		final byte[] dst = new byte[src.length / 3 * 4];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 4) {
			dst[d + 0] = (byte) 0xFF;
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 1];
			dst[d + 3] = src[s + 2];
		}
		return dst;
	}

	private static byte[] convertRGBToGrayscale(byte[] src) {
		final byte[] dst = new byte[src.length / 3];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 1) {
			final var r = src[s + 0];
			final var g = src[s + 1];
			final var b = src[s + 2];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			dst[d] = (byte) luminosity;
		}
		return dst;
	}

}
