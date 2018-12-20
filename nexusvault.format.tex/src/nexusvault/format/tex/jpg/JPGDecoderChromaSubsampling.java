package nexusvault.format.tex.jpg;

import java.util.stream.IntStream;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.StructTextureFileHeader;

final class JPGDecoderChromaSubsampling extends JPGDecoderBase {

	private final int[] layerOffsets = { 0, 4 * Constants.BLOCK_SIZE, 5 * Constants.BLOCK_SIZE, 6 * Constants.BLOCK_SIZE, 10 * Constants.BLOCK_SIZE };
	private final int[] blocksPerLayer = Constants.DECODES_PER_LAYER_TYPE[0];
	private final int maxLayerBlockCount = IntStream.of(blocksPerLayer).max().getAsInt();

	private int numberOfDecodes;
	private int layerBlocksPerRow;
	private int layerBlocksPerColumn;
	private int imageStacksPerRow;
	private int imageStacksPerColumn;

	@Override
	void loadFormatSpecificConfig(StructTextureFileHeader header, ImageMetaInformation meta) {
		final int imageBlocksPerRow = ((meta.width + Constants.BLOCK_WIDTH) - 1) / Constants.BLOCK_WIDTH;
		final int imageBlocksPerColumn = ((meta.height + Constants.BLOCK_HEIGHT) - 1) / Constants.BLOCK_HEIGHT;
		this.layerBlocksPerRow = MathUtil.sqrtInteger(this.maxLayerBlockCount);
		this.layerBlocksPerColumn = layerBlocksPerRow;
		this.imageStacksPerRow = imageBlocksPerRow / layerBlocksPerRow;
		this.imageStacksPerColumn = imageBlocksPerColumn / layerBlocksPerColumn;
		this.numberOfDecodes = imageStacksPerRow * imageStacksPerColumn;
	}

	@Override
	long getNumberOfDecodableStacks() {
		return numberOfDecodes;
	}

	@Override
	StackSet getFreeStack() {
		final StackSet stack = new StackSet(10);
		return stack;
	}

	@Override
	void decodeStack(StackSet stack) {
		final int[] data = stack.data;
		for (int layerId = 0; layerId < Constants.NUMBER_OF_LAYERS; ++layerId) {
			if (!hasLayerDefaultValue(layerId)) {
				for (int i = 0; i < blocksPerLayer[layerId]; ++i) {
					decodeNextBlock(getLayerType(layerId));
					transferDecodeToData(layerId, data, layerOffsets[layerId] + (i * Constants.BLOCK_SIZE));
				}
			} else {
				final int layerStart = layerOffsets[layerId];
				final int layerEnd = layerOffsets[layerId + 1];
				final int defaultValue = getLayerDefaultValue(layerId);
				data[layerStart] = defaultValue;
				for (int i = layerStart + 1; i < layerEnd; i += i) {
					System.arraycopy(data, layerStart, data, i, ((layerEnd - i) < i) ? (layerEnd - i) : i);
				}
			}
		}
	}

	@Override
	void processStack(StackSet stack) {
		// threadsafe version, a bit heavy on the continues ressource allocation?
		processBlockStack(stack, new int[Constants.BLOCK_SIZE]);
	}

	private void processBlockStack(StackSet stack, int[] tmpIDCTBuffer) {
		dequantizate(stack);
		inverseDCT(stack, tmpIDCTBuffer);
		shiftAndClamp(stack);
		// upsambling(stack);
	}

	private void dequantizate(StackSet stack) {
		final int[] data = stack.data;
		for (int layerId = 0; layerId < Constants.NUMBER_OF_LAYERS; ++layerId) {
			if (hasLayerDefaultValue(layerId)) {
				continue;
			}
			for (int i = 0; i < blocksPerLayer[layerId]; ++i) {
				final int blockOffset = layerOffsets[layerId] + (i * Constants.BLOCK_SIZE);
				dequantizate(layerId, data, blockOffset);
			}
		}
	}

	private void inverseDCT(StackSet stack, int[] inverseDCTBuffer) {
		final int[] data = stack.data;
		for (int layerId = 0; layerId < Constants.NUMBER_OF_LAYERS; ++layerId) {
			if (hasLayerDefaultValue(layerId)) {
				continue;
			}
			for (int i = 0; i < blocksPerLayer[layerId]; ++i) {
				final int blockOffset = layerOffsets[layerId] + (i * Constants.BLOCK_SIZE);
				inverseDCT(data, blockOffset, inverseDCTBuffer);
			}
		}
	}

	private void shiftAndClamp(StackSet stack) {
		final int[] data = stack.data;
		for (int layerId = 0; layerId < Constants.NUMBER_OF_LAYERS; ++layerId) {
			if (hasLayerDefaultValue(layerId)) {
				continue;
			}
			for (int i = 0; i < blocksPerLayer[layerId]; ++i) {
				final int blockOffset = layerOffsets[layerId] + (i * Constants.BLOCK_SIZE);
				shiftAndClamp(layerId, data, blockOffset);
			}
		}
	}

	@Override
	void writeStack(StackSet stack) {
		final int stackX = (stack.getId() % imageStacksPerRow);
		final int stackY = (stack.getId() / imageStacksPerRow);
		final int stackOffsetX = stackX * (layerBlocksPerRow * Constants.BLOCK_WIDTH);
		final int stackOffsetY = stackY * (layerBlocksPerColumn * Constants.BLOCK_HEIGHT) * image.getImageWidth();
		final int stackXYOnImage = stackOffsetX + stackOffsetY;
		final int lastImagePixelXY = image.getImageHeight() * image.getImageWidth();

		final int[] blockOffsets = new int[4];

		int imageBlockRowOffset = stackXYOnImage;
		for (int layerBlockColumnIdx = 0; layerBlockColumnIdx < layerBlocksPerColumn; ++layerBlockColumnIdx) {
			int imageBlockColumnOffset = imageBlockRowOffset;
			for (int layerBlockRowIdx = 0; layerBlockRowIdx < layerBlocksPerRow; ++layerBlockRowIdx) {
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

						final int pixelLayerA = stack.data[layerOffsets[0] + blockOffsets[0] + x + (y * Constants.BLOCK_WIDTH)];
						final int pixelLayerB = stack.data[layerOffsets[1] + blockOffsets[1] + (x / 2) + ((y / 2) * Constants.BLOCK_WIDTH)];
						final int pixelLayerC = stack.data[layerOffsets[2] + blockOffsets[2] + (x / 2) + ((y / 2) * Constants.BLOCK_WIDTH)];
						final int pixelLayerD = stack.data[layerOffsets[3] + blockOffsets[3] + x + (y * Constants.BLOCK_WIDTH)];

						getPixelCalculator().compute(image, imageXY, pixelLayerA, pixelLayerB, pixelLayerC, pixelLayerD);
					}
					imageYOffset += image.getImageWidth();
				}
				blockOffsets[0] += Constants.BLOCK_SIZE;
				blockOffsets[1] += Constants.BLOCK_WIDTH / 2; // because of chroma subsampling, each value needs to be read 4 times
				blockOffsets[2] += Constants.BLOCK_WIDTH / 2; // to do so, each each subsampled block gets divided into 4 smaller blocks
				blockOffsets[3] += Constants.BLOCK_SIZE;
				imageBlockColumnOffset += Constants.BLOCK_WIDTH;
			}
			blockOffsets[1] = (Constants.BLOCK_HEIGHT / 2) * Constants.BLOCK_WIDTH;
			blockOffsets[2] = (Constants.BLOCK_HEIGHT / 2) * Constants.BLOCK_WIDTH;
			imageBlockRowOffset += Constants.BLOCK_HEIGHT * image.getImageWidth(); // adding one row of blocks
		}
	}

	@Override
	void returnStack(StackSet stack) {
		// TODO Auto-generated method stub

	}

}
