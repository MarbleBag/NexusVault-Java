package nexusvault.format.tex.jpg.tool.decoder;

import nexusvault.format.tex.TexType;
import nexusvault.format.tex.jpg.tool.MathUtil;

interface PixelWriter {
	void write(int[] src, byte[] dst, int dstIdx);

	static final class IdentityPixelWriter implements PixelWriter {
		@Override
		public void write(int[] src, byte[] dst, int dstIdx) {
			dst[dstIdx + 0] = (byte) MathUtil.clamp(src[0], 0, 0xFF);
			dst[dstIdx + 1] = (byte) MathUtil.clamp(src[1], 0, 0xFF);
			dst[dstIdx + 2] = (byte) MathUtil.clamp(src[2], 0, 0xFF);
			dst[dstIdx + 3] = (byte) MathUtil.clamp(src[3], 0, 0xFF);
		}
	}

	/**
	 * Converts jpg data into argb data without any additional steps in color transformation
	 *
	 * @see TexType#JPEG_TYPE_2
	 */
	static final class PixelWriterYYYY2ARGB implements PixelWriter {
		@Override
		public void write(int[] src, byte[] dst, int dstIdx) {
			dst[dstIdx + 0] = (byte) MathUtil.clamp(src[0], 0, 0xFF);
			dst[dstIdx + 1] = (byte) MathUtil.clamp(src[1], 0, 0xFF);
			dst[dstIdx + 2] = (byte) MathUtil.clamp(src[2], 0, 0xFF);
			dst[dstIdx + 3] = (byte) MathUtil.clamp(src[3], 0, 0xFF);
		}
	}

	/**
	 * Converts jpg data into argb data by applying a color transformation
	 *
	 * @see TexType#JPEG_TYPE_1
	 * @see TexType#JPEG_TYPE_3
	 */
	static final class PixelWriterYCCY2ARGB implements PixelWriter {

		@Override
		public void write(int[] src, byte[] dst, int dstIdx) {
			final int p1 = src[0];
			final int p2 = src[1];
			final int p3 = src[2];
			final int p4 = src[3];

			final int r1 = p1 - (p3 >> 1); // r1 = p1 - p3>>1
			final int r2 = MathUtil.clamp(r1 + p3, 0, 0xFF); // r2 = p1 - p3>>1 + p3
			final int r3 = MathUtil.clamp(r1 - (p2 >> 1), 0, 0xFF); // r3 = p1 - p3>>1 - p2>>1
			final int r4 = MathUtil.clamp(r3 + p2, 0, 0xFF); // r4 = p1 - p3>>1 - p2>>1 + p2

			dst[dstIdx + 0] = (byte) MathUtil.clamp(p4, 0, 0xFF); // A = p4
			dst[dstIdx + 1] = (byte) MathUtil.clamp(r4, 0, 0xFF); // R = p1 - p3/2 - p2/2 + p2
			dst[dstIdx + 2] = (byte) MathUtil.clamp(r2, 0, 0xFF); // G = p1 - p3/2 + p3
			dst[dstIdx + 3] = (byte) MathUtil.clamp(r3, 0, 0xFF); // B = p1 - p3/2 - p2/2
		}

	}
}