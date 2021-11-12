package nexusvault.format.tex.jpg.old;

import java.util.Objects;

import nexusvault.format.tex.jpg.tool.Constants;
import nexusvault.format.tex.jpg.tool.MathUtil;

final class ChromaSubsampleStackWriter implements ImageRegionWriter {

	private final int[] layerOffsets = Constants.OFFSETS_PER_LAYER[0];

	private int imageWidth;
	private int imageHeight;

	private int layerBlocksPerRow;
	private int layerBlocksPerColumn;
	private int imageStacksPerRow;
	private int imageStacksPerColumn;

	private int numberOfSteps;

	private final PixelCompositionStrategy pixelComposition;

	public ChromaSubsampleStackWriter(PixelCompositionStrategy pixelComposition) {
		this.pixelComposition = Objects.requireNonNull(pixelComposition);
	}

	@Override
	public void setImageSize(int imageWidth, int imageHeight) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;

		final int imageBlocksPerRow = (imageWidth + Constants.BLOCK_WIDTH - 1) / Constants.BLOCK_WIDTH;
		final int imageBlocksPerColumn = (imageHeight + Constants.BLOCK_HEIGHT - 1) / Constants.BLOCK_HEIGHT;

		this.layerBlocksPerRow = MathUtil.sqrtInteger(4);
		this.layerBlocksPerColumn = this.layerBlocksPerRow;
		this.imageStacksPerRow = imageBlocksPerRow / this.layerBlocksPerRow;
		this.imageStacksPerColumn = imageBlocksPerColumn / this.layerBlocksPerColumn;

		this.numberOfSteps = this.imageStacksPerRow * this.imageStacksPerColumn;
	}

	@Override
	public int getNumberOfSteps() {
		return this.numberOfSteps;
	}

	@Override
	public void writeImageRegion(StackSet stack, byte[] output) {
		final int stackX = stack.getId() % this.imageStacksPerRow;
		final int stackY = stack.getId() / this.imageStacksPerRow;
		final int stackOffsetX = stackX * this.layerBlocksPerRow * Constants.BLOCK_WIDTH;
		final int stackOffsetY = stackY * this.layerBlocksPerColumn * Constants.BLOCK_HEIGHT * this.imageWidth;
		final int stackXYOnImage = stackOffsetX + stackOffsetY;
		final int lastImagePixelXY = this.imageHeight * this.imageWidth;

		final int[] blockOffsets = new int[4];

		int imageBlockRowOffset = stackXYOnImage;
		for (int layerBlockColumnIdx = 0; layerBlockColumnIdx < this.layerBlocksPerColumn; ++layerBlockColumnIdx) {
			int imageBlockColumnOffset = imageBlockRowOffset;
			for (int layerBlockRowIdx = 0; layerBlockRowIdx < this.layerBlocksPerRow; ++layerBlockRowIdx) {
				int imageYOffset = imageBlockColumnOffset; // start pixel index of the current block. Image and block are represented as one dimensional arrays.

				for (int y = 0; y < Constants.BLOCK_HEIGHT; ++y) {
					if (imageYOffset > lastImagePixelXY) {
						break; // do not compute pixels outside of the image.
					}
					for (int x = 0; x < Constants.BLOCK_WIDTH; ++x) {
						final int imageXY = imageYOffset + x;

						if (imageXY > lastImagePixelXY) {
							break; // do not compute pixels outside of the image.
						}

						final int indexOffset = x + y * Constants.BLOCK_WIDTH;
						final int subsampledIndexOffset = x / 2 + y / 2 * Constants.BLOCK_WIDTH;

						final int pixelLayerA = stack.data[this.layerOffsets[0] + blockOffsets[0] + indexOffset];
						final int pixelLayerB = stack.data[this.layerOffsets[1] + blockOffsets[1] + subsampledIndexOffset];
						final int pixelLayerC = stack.data[this.layerOffsets[2] + blockOffsets[2] + subsampledIndexOffset];
						final int pixelLayerD = stack.data[this.layerOffsets[3] + blockOffsets[3] + indexOffset];

						this.pixelComposition.composite(output, imageXY * 4, pixelLayerA, pixelLayerB, pixelLayerC, pixelLayerD);
					}
					imageYOffset += this.imageWidth;
				}
				blockOffsets[0] += Constants.BLOCK_SIZE;
				blockOffsets[1] += Constants.BLOCK_WIDTH / 2; // because of chroma subsampling, each value needs to be read 4 times
				blockOffsets[2] += Constants.BLOCK_WIDTH / 2; // to do so, each subsampled block gets divided into 4 smaller blocks
				blockOffsets[3] += Constants.BLOCK_SIZE;
				imageBlockColumnOffset += Constants.BLOCK_WIDTH;
			}
			blockOffsets[1] = Constants.BLOCK_HEIGHT / 2 * Constants.BLOCK_WIDTH;
			blockOffsets[2] = Constants.BLOCK_HEIGHT / 2 * Constants.BLOCK_WIDTH;
			imageBlockRowOffset += Constants.BLOCK_HEIGHT * this.imageWidth; // adding one row of blocks
		}
	}

}
