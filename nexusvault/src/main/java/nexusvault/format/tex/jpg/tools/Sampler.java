package nexusvault.format.tex.jpg.tools;

public final class Sampler {
	private Sampler() {
	}

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
	 * @param srcImageRowGap
	 *            in case of interleaved images the number of indices to skip to reach the next row of the image
	 * @param sampleScale
	 *            only supports quadratic upsampling. 2 means 2x2, 3 means 3x3, etc
	 * @param dst
	 *            target array, needs to be big enough to hold the upsampled image.
	 * @param dstOffset
	 *            offet at whicht the image should start
	 * @param dstImageRowGap
	 *            in case of interleaved images the number of indices to skip to reach the next row of the image
	 */
	public static void upsample(int[] src, int srcOffset, int srcImageWidth, int srcImageHeight, int srcImageRowStride, int sampleScale, //
			int[] dst, int dstOffset, int dstImageRowStride) {

		if (srcImageRowStride < srcImageWidth) {
			throw new IllegalArgumentException();
		}

		// final int srcLength = srcImageHeight * srcImageWidth;
		final int dstImageWidth = srcImageWidth * sampleScale;
		final int dstImageHeight = srcImageHeight * sampleScale;

		if (dstImageRowStride < dstImageWidth) {
			throw new IllegalArgumentException();
		}

		// final int dstLength = dstWidth * dstHeight;
		final int srcEndIdx = srcOffset + (srcImageHeight - 1) * srcImageRowStride + srcImageWidth;
		final int dstEndIdx = dstOffset + (dstImageHeight - 1) * dstImageRowStride + dstImageWidth;

		if (src.length < srcEndIdx) {
			throw new IndexOutOfBoundsException(String.format("src too small. Expected %d but was %d", srcEndIdx, src.length));
		}
		if (dst.length < dstEndIdx) {
			throw new IndexOutOfBoundsException(String.format("dst too small. Expected %d but was %d", dstEndIdx, dst.length));
		}

		int srcStartIdx = srcEndIdx;
		int dstStartIdx = dstEndIdx;

		for (int y = 0; y < srcImageHeight; ++y) {
			int srcIdx = srcStartIdx;
			int dstIdx = dstStartIdx;

			for (int x = 0; x < srcImageWidth; ++x) {
				final int sampleValue = src[--srcIdx];
				for (int i = 0; i < sampleScale; ++i) { // fill first row with values
					dst[--dstIdx] = sampleValue;
				}
			}

			for (int i = 1; i < sampleScale; ++i) {
				System.arraycopy(dst, dstIdx, dst, dstIdx - i * dstImageRowStride, dstImageWidth);
			}

			srcStartIdx -= srcImageRowStride;
			dstStartIdx -= sampleScale * dstImageRowStride;
		}
	}

	public static void downsample(int[] src, int srcOffset, int srcImageWidth, int srcImageHeight, int srcImageRowStride, int sampleScale, //
			int[] dst, int dstOffset, int dstImageRowStride) {

		if (srcImageRowStride < srcImageWidth) {
			throw new IllegalArgumentException();
		}

		// final int srcLength = srcImageHeight * srcImageWidth;
		final int dstImageWidth = srcImageWidth / sampleScale;
		final int dstImageHeight = srcImageHeight / sampleScale;

		if (dstImageRowStride < dstImageWidth) {
			throw new IllegalArgumentException();
		}

		// final int dstLength = dstWidth * dstHeight;
		final int srcEndIdx = srcOffset + (srcImageHeight - 1) * srcImageRowStride + srcImageWidth;
		final int dstEndIdx = dstOffset + (dstImageHeight - 1) * dstImageRowStride + dstImageWidth;

		if (src.length < srcEndIdx) {
			throw new IndexOutOfBoundsException(String.format("src too small. Expected %d but was %d", srcEndIdx, src.length));
		}
		if (dst.length < dstEndIdx) {
			throw new IndexOutOfBoundsException(String.format("dst too small. Expected %d but was %d", dstEndIdx, dst.length));
		}

		final double downsampleFactor = 1.0d / (sampleScale * sampleScale);
		int srcStartIdx = srcOffset;
		int dstStartIdx = dstOffset;

		for (int y = 0; y < dstImageHeight; ++y) {
			int srcIdx = srcStartIdx;
			int dstIdx = dstStartIdx;

			for (int x = 0; x < dstImageWidth; ++x) {
				long sum = 0;
				for (int i = 0; i < sampleScale; ++i) {
					for (int j = 0; j < sampleScale; ++j) {
						sum += src[srcIdx + j + i * srcImageRowStride];
					}
				}
				srcIdx += sampleScale;
				dst[dstIdx++] = (int) (sum * downsampleFactor);
			}

			dstStartIdx += dstImageRowStride;
			srcStartIdx += sampleScale * srcImageRowStride;
		}
	}

}
