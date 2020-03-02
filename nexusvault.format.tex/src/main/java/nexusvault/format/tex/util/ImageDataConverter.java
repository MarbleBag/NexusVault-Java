package nexusvault.format.tex.util;

public final class ImageDataConverter {

	public static byte[] convertRGBToBGR(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int d = 0; d < dst.length; d += 3) {
			dst[d + 2] = src[d + 0];
			dst[d + 1] = src[d + 1];
			dst[d + 0] = src[d + 2];
		}
		return dst;
	}

	public static byte[] convertBGRToRGB(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int d = 0; d < dst.length; d += 3) {
			dst[d + 2] = src[d + 0];
			dst[d + 1] = src[d + 1];
			dst[d + 0] = src[d + 2];
		}
		return dst;
	}

	public static byte[] convertARGBToABGR(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int d = 0; d < dst.length; d += 4) {
			dst[d + 0] = src[d + 0];
			dst[d + 3] = src[d + 1];
			dst[d + 2] = src[d + 2];
			dst[d + 1] = src[d + 3];
		}
		return dst;
	}

	public static byte[] convertABGRToARGB(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int d = 0; d < dst.length; d += 4) {
			dst[d + 0] = src[d + 0];
			dst[d + 3] = src[d + 1];
			dst[d + 2] = src[d + 2];
			dst[d + 1] = src[d + 3];
		}
		return dst;
	}

	public static void inplaceConvertARGBToRGBA(byte[] arr) {
		for (int i = 0; i < arr.length; i += 4) {
			final var a = arr[i + 0];
			final var r = arr[i + 1];
			final var g = arr[i + 2];
			final var b = arr[i + 3];
			arr[i + 0] = r;
			arr[i + 1] = g;
			arr[i + 2] = b;
			arr[i + 3] = a;
		}
	}

	public static byte[] convertARGBToGrayscale(byte[] src) {
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

	public static byte[] convertGrayscaleToARGB(byte[] src) {
		final byte[] dst = new byte[src.length * 4];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 4) {
			dst[d + 0] = (byte) 0xFF;
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
			dst[d + 3] = src[s + 0];
		}
		return dst;
	}

	public static byte[] convertARGBToRGB(byte[] src) {
		final byte[] dst = new byte[src.length / 4 * 3];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 3) {
			dst[d + 0] = src[s + 1];
			dst[d + 1] = src[s + 2];
			dst[d + 2] = src[s + 3];
		}
		return dst;
	}

	public static byte[] convertRGBToARGB(byte[] src) {
		final byte[] dst = new byte[src.length / 3 * 4];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 4) {
			dst[d + 0] = (byte) 0xFF;
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 1];
			dst[d + 3] = src[s + 2];
		}
		return dst;
	}

	public static byte[] convertGrayscaleToRGB(byte[] src) {
		final byte[] dst = new byte[src.length * 3];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 3) {
			dst[d + 0] = src[s + 0];
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
		}
		return dst;
	}

	public static byte[] convertRGBToGrayscale(byte[] src) {
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