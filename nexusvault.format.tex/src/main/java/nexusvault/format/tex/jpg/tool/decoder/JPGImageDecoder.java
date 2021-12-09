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
import nexusvault.format.tex.jpg.tool.SampleUtil;
import nexusvault.format.tex.jpg.tool.decoder.PixelWriter.PixelWriterYCCY2ARGB;
import nexusvault.format.tex.jpg.tool.decoder.PixelWriter.PixelWriterYYYY2ARGB;

/**
 * A more compact jpg decoder
 */
public final class JPGImageDecoder {

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
	 *            image
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
				// .sorted((a, b) -> a.id - b.id) //
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
			case JPG1:
			case JPG3:
				return new PixelWriterYCCY2ARGB();
			case JPG2:
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
		dst[startIdx] = this.layerDefault[layerIdx] & 0xFF;
		for (int dstPos = startIdx + 1, length = 1; dstPos < endIdx; dstPos += length, length += length) {
			System.arraycopy(dst, startIdx, dst, dstPos, dstPos + length > endIdx ? endIdx - dstPos : length);
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
		HuffmanDecoder.decode(dc, ac, this.decoderInput, dst, 0, Constants.BLOCK_SIZE);
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
				final var layerOffset = this.layerOffset[layerIdx];
				// this part is hardcoded and only supports 2x2 upsampling, pretty much all that WS needs
				// inplace implementation. Fills layer from back to start
				SampleUtil.upsample(ir.data, layerOffset + 0x24, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 3, 0);
				SampleUtil.upsample(ir.data, layerOffset + 0x20, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 2, 0);
				SampleUtil.upsample(ir.data, layerOffset + 0x04, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 1, 0);
				SampleUtil.upsample(ir.data, layerOffset + 0x00, 4, 4, 4, 2, ir.data, layerOffset + Constants.BLOCK_SIZE * 0, 0);
			}
		}
	}

}
