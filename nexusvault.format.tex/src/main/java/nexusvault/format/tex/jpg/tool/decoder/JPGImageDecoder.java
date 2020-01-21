package nexusvault.format.tex.jpg.tool.decoder;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.jpg.tool.Constants;
import nexusvault.format.tex.jpg.tool.Constants.LayerType;
import nexusvault.format.tex.jpg.tool.FastIntegerIDCT;
import nexusvault.format.tex.jpg.tool.HuffmanTable;
import nexusvault.format.tex.jpg.tool.ImageRegion;
import nexusvault.format.tex.jpg.tool.MathUtil;
import nexusvault.format.tex.jpg.tool.SampleUtil;

/**
 * A more compact jpg decoder
 */
final class JPGImageDecoder {

	private static interface PixelWriter {
		void write(int[] src, byte[] dst, int dstIdx);
	}

	private static final class IdentityPixelWriter implements PixelWriter {
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
	private static final class PixelWriterYYYY2ARGB implements PixelWriter {
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
	private static final class PixelWriterYCCY2ARGB implements PixelWriter {

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

	private static final class ImageRegionWriter {

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
			final int regionY = regionIdx / this.regionsPerImageColumn;

			final int dstIdxStart = regionX * this.regionWidth + regionY * this.regionHeight * this.imageWidth; // start pixel index
			// final int dstIdxEnd = this.imageHeight * this.imageWidth; // last pixel + 1

			final int imageX = regionX * this.regionWidth;
			final int imageY = regionY * this.regionHeight;

			final int[] tmpStore = new int[Constants.NUMBER_OF_LAYERS];

			if (imageX + this.regionWidth > this.imageHeight || imageY + this.regionHeight > this.imageHeight) {
				// region overlaps with image borders
				// borders need to be checked so only pixels within the image will be accessed
				for (int y = 0; y < this.regionHeight; ++y) {
					if (y + imageY > this.imageHeight) {
						break;
					}

					for (int x = 0; x < this.regionWidth; ++x) {
						if (x + imageX > this.imageWidth) {
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
			} else {
				// region is contained within image
				// no additional checks for borders are needed
				for (int y = 0; y < this.regionHeight; ++y) {
					for (int x = 0; x < this.regionWidth; ++x) {
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
		}

		public int getTotalNumberOfImageRegions() {
			return this.totalNumberOfRegions;
		}
	}

	private final HuffmanDecoder decoder = new HuffmanDecoder();
	private final int[] tmpBlockStorage = new int[Constants.BLOCK_SIZE];

	private final TexType target;

	private final LayerType[] layerType;
	private final int[] layerBlocks;
	private final int[] layerOffset;
	private final int[] layerDCValues;
	private final float[][] layerQuantTable;

	private final boolean[] isLayerSubsampled; // 2x2 subsample
	private final boolean[] layerHasDefault;
	private final byte[] layerDefault;

	private final boolean isUsingSubsampling;
	private final int imageRegionSize;

	private int lastImageRegionId;
	private BitSupply decoderInput;
	private ImageRegionWriter imageWriter;

	private byte[] output;

	public JPGImageDecoder(TexType target, boolean[] hasDefault, byte[] defaultValue, float[][] quantTables) {
		final int constantIdx = Constants.getArrayIndexForType(target);
		this.target = target;
		this.layerType = Constants.TYPE_PER_LAYER[constantIdx];
		this.layerBlocks = Constants.BLOCKS_PER_LAYER[constantIdx];
		this.layerOffset = Constants.OFFSETS_PER_LAYER2[constantIdx];
		this.layerDCValues = new int[Constants.NUMBER_OF_LAYERS];
		this.layerQuantTable = quantTables;

		this.isLayerSubsampled = Constants.LAYER_IS_SUBSAMPLED[constantIdx];
		this.layerHasDefault = hasDefault;
		this.layerDefault = defaultValue;

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

	/**
	 *
	 * @param input
	 * @param imageWidth
	 *            width of the image in pixel
	 * @param imageHeight
	 *            height of the image in pixel
	 * @return decoded input as a row-major matrix, four bytes per element
	 */
	public byte[] decode(BinaryReader input, int imageWidth, int imageHeight) {
		this.lastImageRegionId = 0;
		this.decoderInput = new BinaryReaderBitSupplier(input, Integer.MAX_VALUE /* inputLengthInBytes */);
		this.output = new byte[imageWidth * imageHeight * TextureImageFormat.ARGB.getBytesPerPixel()];
		this.imageWriter = new ImageRegionWriter(getPixelWriter(), imageWidth, imageHeight, this.isUsingSubsampling);

		for (int i = 0; i < Constants.NUMBER_OF_LAYERS; ++i) {
			this.layerDCValues[i] = 0;
		}

		Stream.generate(this::allocateMemory) //
				.limit(this.imageWriter.getTotalNumberOfImageRegions()) //
				.peek(this::decodeImageRegion) //
				.collect(Collectors.toList()) // sequential -> parallel
				.parallelStream() //
				.peek(this::convertImageRegion) //
				.peek(this::upsampleImageRegion) //
				.forEach(ir -> this.imageWriter.write(ir.data, ir.id, this.output));

		this.decoderInput = null;

		final var outputTmp = this.output;
		this.output = null;
		return outputTmp;
	}

	private PixelWriter getPixelWriter() {
		switch (this.target) {
			case JPEG_TYPE_1:
			case JPEG_TYPE_3:
				return new PixelWriterYCCY2ARGB();
			case JPEG_TYPE_2:
				return new PixelWriterYYYY2ARGB();
			default:
				throw new IllegalArgumentException("TexType " + this.target + " is not supported");
		}
	}

	private ImageRegion allocateMemory() {
		return new ImageRegion(this.lastImageRegionId++, new int[this.imageRegionSize]);
	}

	private void decodeImageRegion(ImageRegion ir) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (this.layerHasDefault[layerIdx]) {
				copyDefaultValue(layerIdx, ir.data);
			} else {
				decodeLayer(layerIdx, ir.data);
			}
		}
	}

	private void copyDefaultValue(int layerIdx, int[] dst) {
		final var startIdx = this.layerOffset[layerIdx];
		final var endIdx = this.layerOffset[layerIdx + 1];
		dst[startIdx] = this.layerDefault[layerIdx];
		for (int i = startIdx + 1; i < endIdx; i += i) {
			System.arraycopy(dst, startIdx, dst, i, endIdx - i < i ? endIdx - i : i);
		}
	}

	private void decodeLayer(int layerIdx, int[] dst) {
		for (int blockIdx = 0; blockIdx < this.layerBlocks[layerIdx]; ++blockIdx) {
			final int dataOffset = this.layerOffset[layerIdx] + blockIdx * Constants.BLOCK_SIZE;
			decodeBlock(layerIdx, this.tmpBlockStorage);
			applyReverseZigZag(this.tmpBlockStorage, dst, dataOffset);
			adjustBlockDC(layerIdx, dst, dataOffset);
		}
	}

	private void decodeBlock(int layerIdx, int[] dst) {
		final LayerType type = this.layerType[layerIdx];
		final HuffmanTable dc = Constants.getHuffmanTable(type, 0);
		final HuffmanTable ac = Constants.getHuffmanTable(type, 1);
		this.decoder.decode(dc, ac, this.decoderInput, dst, 0, Constants.BLOCK_SIZE);
	}

	private void applyReverseZigZag(int[] src, int[] dst, int dstOffset) {
		for (int i = dstOffset, n = 0; i < dstOffset + Constants.BLOCK_SIZE; ++i, ++n) {
			dst[i] = src[Constants.ZIGZAG[n]];
		}
	}

	private void adjustBlockDC(int layerIdx, int[] arr, int arrOffset) {
		final int prevDC = this.layerDCValues[layerIdx];
		final int blockDC = arr[arrOffset];
		final int newDC = blockDC + prevDC;
		this.layerDCValues[layerIdx] = newDC;
		arr[arrOffset] = newDC;
	}

	private void convertImageRegion(ImageRegion ir) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (!this.layerHasDefault[layerIdx]) {
				for (int blockIdx = 0; blockIdx < this.layerBlocks[layerIdx]; ++blockIdx) {
					final int dataOffset = this.layerOffset[layerIdx] + blockIdx * Constants.BLOCK_SIZE;
					dequantizateBlock(this.layerQuantTable[layerIdx], ir.data, dataOffset);
					inverseDCT(ir.data, dataOffset);
					shiftAndClamp(layerIdx, ir.data, dataOffset);
				}
			}
		}
	}

	private void dequantizateBlock(float[] quantTable, int[] dst, int dstOffset) {
		final int blockStart = dstOffset;
		final int blockEnd = blockStart + Constants.BLOCK_SIZE;
		for (int blockIdx = blockStart, quantIdx = 0; blockIdx < blockEnd; ++blockIdx, ++quantIdx) {
			dst[blockIdx] = Math.round(dst[blockIdx] * quantTable[quantIdx]);
		}
	}

	private void inverseDCT(int[] data, int dataOffset) {
		FastIntegerIDCT.idct(data, dataOffset);
	}

	private void shiftAndClamp(int layerIdx, int[] data, int dataOffset) {
		switch (this.layerType[layerIdx]) {
			case CHROMA:
				shiftAndClamp(data, dataOffset, 0, -256, 255);
				break;
			case LUMINANCE:
				shiftAndClamp(data, dataOffset, 128, 0, 255);
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

	private void upsampleImageRegion(ImageRegion ir) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (this.isLayerSubsampled[layerIdx] && !this.layerHasDefault[layerIdx]) {
				final var layerOffset = this.layerOffset[layerIdx]; // this part is hardcoded and only supports 2x2 upsampling, pretty much all that WS needs
				// start at the end and work back to the start, because it's done inplace
				SampleUtil.upsample(ir.data, layerOffset + 0x24, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 3, 0);
				SampleUtil.upsample(ir.data, layerOffset + 0x20, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 2, 0);
				SampleUtil.upsample(ir.data, layerOffset + 0x04, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 1, 0);
				SampleUtil.upsample(ir.data, layerOffset + 0x00, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 0, 0);
			}
		}
	}

}
