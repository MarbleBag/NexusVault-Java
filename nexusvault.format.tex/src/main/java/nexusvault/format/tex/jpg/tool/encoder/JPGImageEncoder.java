package nexusvault.format.tex.jpg.tool.encoder;

import java.util.stream.Stream;

import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.jpg.TextureJPGEncodingException;
import nexusvault.format.tex.jpg.tool.Constants;
import nexusvault.format.tex.jpg.tool.Constants.LayerType;
import nexusvault.format.tex.jpg.tool.FastDCT;
import nexusvault.format.tex.jpg.tool.HuffmanTable;
import nexusvault.format.tex.jpg.tool.ImageRegion;
import nexusvault.format.tex.jpg.tool.MathUtil;
import nexusvault.format.tex.jpg.tool.SampleUtil;

public final class JPGImageEncoder {

	private static interface PixelReader {
		void read(byte[] image, int imageIdx, int[] out);
	}

	private static final class IdentityPixelReader implements PixelReader {
		@Override
		public void read(byte[] image, int imageIdx, int[] out) {
			out[0] = image[imageIdx + 0] & 0xFF;
			out[1] = image[imageIdx + 1] & 0xFF;
			out[2] = image[imageIdx + 2] & 0xFF;
			out[3] = image[imageIdx + 3] & 0xFF;
		}
	}

	private static final class PixelReaderARGB2YYYY implements PixelReader {
		@Override
		public void read(byte[] src, int srcIdx, int[] dst) {
			dst[0] = src[srcIdx + 0] & 0xFF;
			dst[1] = src[srcIdx + 1] & 0xFF;
			dst[2] = src[srcIdx + 2] & 0xFF;
			dst[3] = src[srcIdx + 3] & 0xFF;
		}
	}

	private static final class PixelReaderARGB2YCCY implements PixelReader {
		@Override
		public void read(byte[] src, int srcIdx, int[] dst) {
			final int p4 = src[srcIdx + 0] & 0xFF;
			final int r4 = src[srcIdx + 1] & 0xFF;
			final int r2 = src[srcIdx + 2] & 0xFF;
			final int r3 = src[srcIdx + 3] & 0xFF;

			final int p2 = r4 - r3; // r4 = r3 + p2
			final int r1 = r3 + (p2 >> 1); // r3 = r1 - p2>>1
			final int p3 = r2 - r1; // r2 = r1 + p3
			final int p1 = r1 + (p3 >> 1); // r1 = p1 - p3>>1

			dst[0] = MathUtil.clamp(p1, -256, 0xFF);
			dst[1] = MathUtil.clamp(p2, -256, 0xFF);
			dst[2] = MathUtil.clamp(p3, -256, 0xFF);
			dst[3] = MathUtil.clamp(p4, -256, 0xFF);
		}
	}

	private static final class ImageRegionReader {

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

		private final PixelReader reader;

		public ImageRegionReader(PixelReader reader, int width, int height, boolean subsampled) {
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

			this.reader = reader;
		}

		public void read(byte[] image, int regionIdx, int[] out) {
			final int regionX = regionIdx % this.regionsPerImageRow;
			final int regionY = regionIdx / this.regionsPerImageColumn;

			final int srcIdxStart = regionX * this.regionWidth + regionY * this.regionHeight * this.imageWidth; // start pixel index
			// final int dstIdxEnd = this.imageHeight * this.imageWidth; // last pixel + 1

			final int imageX = regionX * this.regionWidth;
			final int imageY = regionY * this.regionHeight;

			final int[] tmpStore = new int[Constants.NUMBER_OF_LAYERS];

			for (int y = 0; y < this.regionHeight; ++y) {

				for (int x = 0; x < this.regionWidth; ++x) {
					final int dstIdx = this.regionLookup[y][x];
					final int srcIdx = this.imageLookup[y][x] + srcIdxStart;

					if (x + imageX < this.imageWidth && y + imageY < this.imageHeight) {
						// TODO not perfect. On the right border we should reuse the last value read from the line
						// On the bottom border we should replicate the last line
						this.reader.read(image, srcIdx * 4, tmpStore);
					}

					out[dstIdx + 0 * this.regionSize] = tmpStore[0];
					out[dstIdx + 1 * this.regionSize] = tmpStore[1];
					out[dstIdx + 2 * this.regionSize] = tmpStore[2];
					out[dstIdx + 3 * this.regionSize] = tmpStore[3];
				}
			}
		}

		public int getTotalNumberOfImageRegions() {
			return this.totalNumberOfRegions;
		}
	}

	private final HuffmanEncoder encoder = new HuffmanEncoder();
	private final int[] tmpBlockStorage = new int[Constants.BLOCK_SIZE];

	private final TexType target;

	private final LayerType[] layerType;
	private final int[] layerBlocks;
	private final int[] layerOffset;
	private final int[] layerDCValues;
	private final float[][] layerQuantTable;

	private final boolean[] isLayerSubsampled; // 2x2 subsample
	private final boolean[] layerHasDefault;

	private final boolean isUsingSubsampling;
	private final int imageRegionSize;

	private int lastImageRegionId;
	private BinaryWriterBitConsumer encoderOutput;
	private ImageRegionReader imageReader;

	public JPGImageEncoder(TexType target, boolean[] hasDefault, float[][] quantTables) {
		final int constantIdx = Constants.getArrayIndexForType(target);
		this.target = target;
		this.layerType = Constants.TYPE_PER_LAYER[constantIdx];
		this.layerBlocks = Constants.BLOCKS_PER_LAYER[constantIdx];
		this.layerOffset = Constants.OFFSETS_PER_LAYER2[constantIdx];
		this.layerDCValues = new int[Constants.NUMBER_OF_LAYERS];
		this.layerQuantTable = quantTables;

		this.isLayerSubsampled = Constants.LAYER_IS_SUBSAMPLED[constantIdx];
		this.layerHasDefault = hasDefault;

		this.isUsingSubsampling = arrayAny(this.isLayerSubsampled);
		this.imageRegionSize = this.layerOffset[this.layerOffset.length - 1];
	}

	private boolean arrayAny(boolean[] arr) {
		for (final var b : arr) {
			if (b) {
				return true;
			}
		}
		return false;
	}

	public void encode(byte[] image, int imageWidth, int imageHeight, BinaryWriter out) {
		if (image.length != imageWidth * imageHeight * 4) {
			throw new IllegalArgumentException(); // TODO
		}

		this.lastImageRegionId = 0;
		this.encoderOutput = new BinaryWriterBitConsumer(out);
		this.imageReader = new ImageRegionReader(getPixelReader(), imageWidth, imageHeight, this.isUsingSubsampling);

		for (int i = 0; i < Constants.NUMBER_OF_LAYERS; ++i) {
			this.layerDCValues[i] = 0;
		}

		Stream.generate(this::allocateMemory) // //TODO
				.limit(this.imageReader.getTotalNumberOfImageRegions()) //
				// .parallel() //
				.peek(ir -> {
					try {
						this.imageReader.read(image, ir.id, ir.data);
					} catch (final Exception ex) {
						throw new TextureJPGEncodingException(String.format("Region [%d]: read error", ir.id), ex);
					}
				}) //
				.peek(this::downsampleImageRegion) //
				.peek(this::convertImageRegion) //
				// .collect(Collectors.toList()) // parallel -> sequential
				// .stream() //
				// .sorted(this::sortImageRegion) //
				.forEach(this::encodeImageRegion);

		this.encoderOutput.flush();
		this.encoderOutput = null;
	}

	private PixelReader getPixelReader() {
		switch (this.target) {
			case JPEG_TYPE_1:
			case JPEG_TYPE_3:
				return new PixelReaderARGB2YCCY();
			case JPEG_TYPE_2:
				return new PixelReaderARGB2YYYY();
			default:
				throw new IllegalArgumentException("TexType " + this.target + " is not supported");
		}
	}

	private ImageRegion allocateMemory() {
		return new ImageRegion(this.lastImageRegionId++, new int[this.imageRegionSize]);
	}

	private void downsampleImageRegion(ImageRegion ir) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (this.isLayerSubsampled[layerIdx] && !this.layerHasDefault[layerIdx]) {
				final var layerOffset = this.layerOffset[layerIdx];
				SampleUtil.downsample(ir.data, layerOffset + 0 * Constants.BLOCK_SIZE, Constants.BLOCK_WIDTH, Constants.BLOCK_HEIGHT, 0, 2, ir.data, 0x00, 4);
				SampleUtil.downsample(ir.data, layerOffset + 1 * Constants.BLOCK_SIZE, Constants.BLOCK_WIDTH, Constants.BLOCK_HEIGHT, 0, 2, ir.data, 0x04, 4);
				SampleUtil.downsample(ir.data, layerOffset + 2 * Constants.BLOCK_SIZE, Constants.BLOCK_WIDTH, Constants.BLOCK_HEIGHT, 0, 2, ir.data, 0x20, 4);
				SampleUtil.downsample(ir.data, layerOffset + 3 * Constants.BLOCK_SIZE, Constants.BLOCK_WIDTH, Constants.BLOCK_HEIGHT, 0, 2, ir.data, 0x24, 4);
			}
		}
	}

	private void convertImageRegion(ImageRegion ir) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (!this.layerHasDefault[layerIdx]) {
				for (int blockIdx = 0; blockIdx < this.layerBlocks[layerIdx]; ++blockIdx) {
					final int blockOffset = this.layerOffset[layerIdx] + blockIdx * Constants.BLOCK_SIZE;
					shiftAndClamp(layerIdx, ir.data, blockOffset);
					DCT(ir.data, blockOffset); // TODO this part still sucks
					quantizate(layerIdx, ir.data, blockOffset);
				}
			}
		}
	}

	private void shiftAndClamp(int layerIdx, int[] data, int dataOffset) {
		switch (this.layerType[layerIdx]) {
			case CHROMA:
				shiftAndClamp(data, dataOffset, 0, -256, 255);
				break;
			case LUMINANCE:
				shiftAndClamp(data, dataOffset, -128, -256, 255);
				break;
			default:
				throw new IllegalArgumentException("Unknown type: " + this.layerType[layerIdx]);
		}
	}

	private void shiftAndClamp(int[] data, int offset, int shift, int min, int max) {
		for (int i = offset; i < offset + Constants.BLOCK_SIZE; ++i) {
			data[i] = Math.max(min, Math.min(max, data[i] + shift));
		}
	}

	private void DCT(int[] data, int dataOffset) {
		FastDCT.dct(data, dataOffset);
	}

	private void quantizate(int layerIdx, int[] arr, int arrOffset) {
		final float[] quantTable = this.layerQuantTable[layerIdx];
		final int blockStart = arrOffset;
		final int blockEnd = blockStart + Constants.BLOCK_SIZE;

		for (int blockIdx = blockStart, quantIdx = 0; blockIdx < blockEnd; ++blockIdx, ++quantIdx) {
			arr[blockIdx] = Math.round(arr[blockIdx] / quantTable[quantIdx]);
		}
	}

	private int sortImageRegion(ImageRegion a, ImageRegion b) {
		return Integer.compare(a.id, b.id);
	}

	private void encodeImageRegion(ImageRegion ir) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (!this.layerHasDefault[layerIdx]) {
				try {
					encodeLayer(layerIdx, ir.data);
				} catch (final HuffmanEncoderFault ex) {
					throw new TextureJPGEncodingException(String.format("Region [%d] Layer [%d]: encoding error", ir.id, layerIdx), ex);
				}
			}
		}
	}

	private void encodeLayer(int layerIdx, int[] data) {
		for (int blockIdx = 0; blockIdx < this.layerBlocks[layerIdx]; ++blockIdx) {
			final int dataOffset = this.layerOffset[layerIdx] + blockIdx * Constants.BLOCK_SIZE;
			adjustBlockDC(layerIdx, data, dataOffset);
			applyZigZag(data, dataOffset, this.tmpBlockStorage);
			encodeBlock(layerIdx, this.tmpBlockStorage);
		}
	}

	private void adjustBlockDC(int layerIdx, int[] arr, int arrOffset) {
		final int prevDC = this.layerDCValues[layerIdx];
		final int blockDC = arr[arrOffset];
		final int newDC = blockDC - prevDC;
		this.layerDCValues[layerIdx] = newDC;
		arr[arrOffset] = newDC;
	}

	private void applyZigZag(int[] src, int srcOffset, int[] dst) {
		for (int i = srcOffset, n = 0; i < srcOffset + Constants.BLOCK_SIZE; ++i, ++n) {
			dst[Constants.ZIGZAG[n]] = src[i];
		}
	}

	private void encodeBlock(int layerIdx, int[] dst) {
		final LayerType type = this.layerType[layerIdx];
		final HuffmanTable dc = Constants.getHuffmanTable(type, 0);
		final HuffmanTable ac = Constants.getHuffmanTable(type, 1);
		this.encoder.encode(dc, ac, this.encoderOutput, dst, 0, Constants.BLOCK_SIZE);
	}

}
