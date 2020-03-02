package nexusvault.format.tex.jpg.tool;

public final class SampleUtil {

	/**
	 *
	 * @param src
	 *            image array, can contain multiple images
	 * @param srcOffset
	 *            offset at which the image starts
	 * @param srcImageHeight
	 *            height of the image
	 * @param srcImageWidth
	 *            width of the image
	 * @param srcImageRowInterleave
	 *            in case of interleaved images the number of indices to skip to reach the next row of the image
	 * @param sampleScale
	 *            only supports quadratic upsampling. 2 means 2x2, 3 means 3x3, etc
	 * @param dst
	 *            target array, needs to be big enough to hold the upsampled image.
	 * @param dstOffset
	 *            offet at whicht the image should start
	 * @param dstImageRowInterleave
	 *            in case of interleaved images the number of indices to skip to reach the next row of the image
	 */
	public static void upsample(int[] src, int srcOffset, int srcImageWidth, int srcImageHeight, int srcImageRowInterleave, int sampleScale, //
			int[] dst, int dstOffset, int dstImageRowInterleave) {

		// final int srcLength = srcImageHeight * srcImageWidth;
		final int dstWidth = srcImageWidth * sampleScale;
		final int dstHeight = srcImageWidth * sampleScale;
		// final int dstLength = dstWidth * dstHeight;
		final int srcEndIdx = srcOffset + srcImageWidth + (srcImageHeight - 1) * (srcImageRowInterleave + srcImageWidth);
		final int dstEndIdx = dstOffset + dstWidth + (dstHeight - 1) * (dstImageRowInterleave + dstWidth);

		if (src.length < srcEndIdx) {
			throw new IndexOutOfBoundsException(String.format("src too small. Expected %d but was %d", srcEndIdx, src.length));
		}
		if (dst.length < dstEndIdx) {
			throw new IndexOutOfBoundsException(String.format("dst too small. Expected %d but was %d", dstEndIdx, dst.length));
		}

		int srcWPos = srcImageWidth;
		int srcStartIdx = srcEndIdx - 1;
		int dstStartIdx = dstEndIdx - 1;
		int srcIdx = srcStartIdx;
		int dstIdx = dstStartIdx;
		for (; srcIdx >= srcOffset; --srcIdx) {
			final int sampleValue = src[srcIdx];

			for (int i = 0; i < sampleScale; ++i) { // fill first row with values
				dst[dstIdx--] = sampleValue;
			}
			for (int i = 1; i < sampleScale; ++i) { // copy row to the rows above
				System.arraycopy(dst, dstIdx + 1, dst, dstIdx + 1 - i * (dstWidth + dstImageRowInterleave), sampleScale);
			}

			if (--srcWPos == 0) { // move to next row
				srcWPos = srcImageWidth;
				srcStartIdx = srcStartIdx - (srcImageWidth + srcImageRowInterleave);
				srcIdx = srcStartIdx + 1;
				dstStartIdx = dstStartIdx - sampleScale * (dstWidth + dstImageRowInterleave);
				dstIdx = dstStartIdx;
			}
		}
	}

	public static void downsample(int[] src, int srcOffset, int srcImageWidth, int srcImageHeight, int srcImageRowInterleave, int sampleScale, //
			int[] dst, int dstOffset, int dstImageRowInterleave) {

		// final int srcLength = srcImageHeight * srcImageWidth;
		final int dstWidth = srcImageWidth / sampleScale;
		final int dstHeight = srcImageWidth / sampleScale;
		// final int dstLength = dstWidth * dstHeight;

		final int srcEndIdx = srcOffset + srcImageWidth - 1 + (srcImageHeight - 1) * (srcImageRowInterleave + srcImageWidth);
		final int dstEndIdx = dstOffset + dstWidth - 1 + (dstHeight - 1) * (dstImageRowInterleave + dstWidth);

		if (src.length <= srcEndIdx) {
			throw new IndexOutOfBoundsException(String.format("src too small. Expected %d but was %d", srcEndIdx, src.length));
		}
		if (dst.length <= dstEndIdx) {
			throw new IndexOutOfBoundsException(String.format("dst too small. Expected %d but was %d", dstEndIdx, dst.length));
		}

		final double downsampleValue = sampleScale * sampleScale;
		int dstWPos = dstWidth;
		int srcIdx = srcOffset;
		int dstIdx = dstOffset;
		for (; dstIdx < dstEndIdx; ++dstIdx) {
			int sumA = 0;
			int sumB = 0;
			int sumC = 0;
			int sumD = 0;

			for (int i = 0; i < sampleScale; ++i) {
				for (int j = 0; j < sampleScale; ++j) {
					final int srcData = src[srcIdx + j + i * (srcImageWidth + srcImageRowInterleave)];
					sumA += srcData >> 24 & 0xFF;
					sumB += srcData >> 16 & 0xFF;
					sumC += srcData >> 8 & 0xFF;
					sumD += srcData >> 0 & 0xFF;
				}
			}
			srcIdx += sampleScale;

			final int sampleA = (int) (sumA / downsampleValue) & 0xFF;
			final int sampleB = (int) (sumB / downsampleValue) & 0xFF;
			final int sampleC = (int) (sumC / downsampleValue) & 0xFF;
			final int sampleD = (int) (sumD / downsampleValue) & 0xFF;
			final int sampleValue = sampleA << 24 | sampleB << 16 | sampleC << 8 | sampleD;
			dst[dstIdx] = sampleValue;

			if (--dstWPos == 0) { // move to next row
				dstWPos = dstWidth;
				dstIdx += dstImageRowInterleave;
				srcIdx += sampleScale * (srcImageWidth + srcImageRowInterleave) - srcImageWidth;
			}
		}
	}

}
