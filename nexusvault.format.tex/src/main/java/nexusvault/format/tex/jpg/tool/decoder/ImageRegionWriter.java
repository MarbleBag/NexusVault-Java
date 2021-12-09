package nexusvault.format.tex.jpg.tool.decoder;

import nexusvault.format.tex.jpg.tool.Constants;

final class ImageRegionWriter {

	private static void coordinatesToRegionIdx(int[][] lookup, int x, int y, int start) {
		for (int y1 = y, idx = start; y1 < Constants.BLOCK_HEIGHT + y; ++y1) {
			for (int x1 = x; x1 < Constants.BLOCK_WIDTH + x; ++x1) {
				lookup[y1][x1] = idx++;
			}
		}
	}

	private static final int[][] SINGLE_LOOKUP;
	private static final int[][] MULTI_LOOKUP;

	static {
		SINGLE_LOOKUP = new int[Constants.BLOCK_HEIGHT][Constants.BLOCK_WIDTH];
		coordinatesToRegionIdx(SINGLE_LOOKUP, 0, 0, 0);

		MULTI_LOOKUP = new int[Constants.BLOCK_HEIGHT * 2][Constants.BLOCK_WIDTH * 2];
		coordinatesToRegionIdx(MULTI_LOOKUP, 0 * Constants.BLOCK_WIDTH, 0 * Constants.BLOCK_HEIGHT, 0 * Constants.BLOCK_SIZE);
		coordinatesToRegionIdx(MULTI_LOOKUP, 1 * Constants.BLOCK_WIDTH, 0 * Constants.BLOCK_HEIGHT, 1 * Constants.BLOCK_SIZE);
		coordinatesToRegionIdx(MULTI_LOOKUP, 0 * Constants.BLOCK_WIDTH, 1 * Constants.BLOCK_HEIGHT, 2 * Constants.BLOCK_SIZE);
		coordinatesToRegionIdx(MULTI_LOOKUP, 1 * Constants.BLOCK_WIDTH, 1 * Constants.BLOCK_HEIGHT, 3 * Constants.BLOCK_SIZE);
	}

	private final int imageWidth;
	private final int imageHeight;
	private final int regionWidth;
	private final int regionHeight;
	private final int regionSize;
	private final int regionsPerImageRow;
	private final int regionsPerImageColumn;
	private final int totalNumberOfRegions;

	private final int[][] regionLookup;
	private final int[][] imageLookup;

	private final PixelWriter writer;

	public ImageRegionWriter(PixelWriter writer, int width, int height, boolean subsampled) {
		this.imageWidth = width;
		this.imageHeight = height;
		this.regionWidth = subsampled ? Constants.BLOCK_WIDTH * 2 : Constants.BLOCK_WIDTH;
		this.regionHeight = subsampled ? Constants.BLOCK_HEIGHT * 2 : Constants.BLOCK_HEIGHT;
		this.regionSize = this.regionWidth * this.regionHeight;
		this.regionsPerImageRow = (this.imageWidth + this.regionWidth - 1) / this.regionWidth;
		this.regionsPerImageColumn = (this.imageHeight + this.regionHeight - 1) / this.regionHeight;
		this.totalNumberOfRegions = this.regionsPerImageRow * this.regionsPerImageColumn;

		this.regionLookup = subsampled ? MULTI_LOOKUP : SINGLE_LOOKUP;

		// precalculate indices, those will be reused - a lot
		this.imageLookup = new int[this.regionHeight][this.regionWidth];
		for (int y = 0; y < this.regionHeight; ++y) {
			for (int x = 0; x < this.regionWidth; ++x) {
				this.imageLookup[y][x] = x + y * this.imageWidth;
			}
		}

		this.writer = writer;
	}

	public void write(int[] src, int regionIdx, byte[] dst) {
		final int regionX = regionIdx % this.regionsPerImageRow;
		final int regionY = regionIdx / this.regionsPerImageRow;

		final int dstIdxStart = regionX * this.regionWidth + regionY * this.regionHeight * this.imageWidth; // start pixel index
		// final int dstIdxEnd = this.imageHeight * this.imageWidth; // last pixel + 1

		final int imageX = regionX * this.regionWidth;
		final int imageY = regionY * this.regionHeight;

		final int[] tmpStore = new int[Constants.NUMBER_OF_LAYERS];

		for (int y = 0; y < this.regionHeight; ++y) {
			if (y + imageY >= this.imageHeight) {
				break;
			}

			for (int x = 0; x < this.regionWidth; ++x) {
				if (x + imageX >= this.imageWidth) {
					break;
				}

				final int srcIdx = this.regionLookup[y][x];
				final int dstIdx = this.imageLookup[y][x] + dstIdxStart;

				tmpStore[0] = src[srcIdx + 0 * this.regionSize];
				tmpStore[1] = src[srcIdx + 1 * this.regionSize];
				tmpStore[2] = src[srcIdx + 2 * this.regionSize];
				tmpStore[3] = src[srcIdx + 3 * this.regionSize];

				this.writer.write(tmpStore, dst, dstIdx * 4);
			}
		}
	}

	public int getTotalNumberOfImageRegions() {
		return this.totalNumberOfRegions;
	}
}