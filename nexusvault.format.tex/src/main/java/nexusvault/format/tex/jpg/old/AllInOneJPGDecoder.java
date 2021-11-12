package nexusvault.format.tex.jpg.old;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.jpg.tool.Constants;
import nexusvault.format.tex.jpg.tool.Constants.LayerType;
import nexusvault.format.tex.jpg.tool.FastIntegerIDCT;
import nexusvault.format.tex.jpg.tool.HuffmanTable;
import nexusvault.format.tex.jpg.tool.decoder.BinaryReaderBitSupplier;
import nexusvault.format.tex.jpg.tool.decoder.BitSupply;
import nexusvault.format.tex.jpg.tool.decoder.HuffmanDecoder;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class AllInOneJPGDecoder {

	private final int[] decoderOutput = new int[Constants.BLOCK_SIZE];
	private final int[] dcValues = new int[Constants.NUMBER_OF_LAYERS];

	private final boolean[] hasDefaultForLayer = new boolean[Constants.NUMBER_OF_LAYERS];
	private final byte[] defaultForLayer = new byte[Constants.NUMBER_OF_LAYERS];

	private LayerType[] typePerLayer;
	private int[] offsetsPerLayer;
	private int[] blocksPerLayer;

	private float[][] quantTables;

	private ImageRegionWriter stackWriter;
	private BitSupply source;
	private byte[] output;
	private int lastStackId;

	private StackResourceHandler stackResourceHandler;

	public AllInOneJPGDecoder() {

	}

	public final byte[] decode(StructTextureFileHeader header, BinaryReader data, int byteLength, int width, int height) {
		final var texType = TexType.resolve(header);

		final int constantIdx = Constants.getArrayIndexForType(texType);
		this.typePerLayer = Constants.TYPE_PER_LAYER[constantIdx];
		this.blocksPerLayer = Constants.BLOCKS_PER_LAYER[constantIdx];
		this.offsetsPerLayer = Constants.OFFSETS_PER_LAYER[constantIdx];
		this.quantTables = new float[Constants.NUMBER_OF_LAYERS][];

		this.stackWriter = getImageRegionWriter(texType);
		this.stackWriter.setImageSize(width, height);
		this.source = new BinaryReaderBitSupplier(data, byteLength);
		this.output = new byte[width * height * TextureImageFormat.ARGB.getBytesPerPixel()];
		this.lastStackId = 0;

		for (int i = 0; i < Constants.NUMBER_OF_LAYERS; ++i) {
			final var layerInfo = header.layerInfos[i];
			this.dcValues[i] = 0;
			this.quantTables[i] = Constants.getAdjustedQuantTable(this.typePerLayer[i], layerInfo.getQuality());
			this.hasDefaultForLayer[i] = layerInfo.hasReplacement();
			if (this.hasDefaultForLayer[i]) {
				this.defaultForLayer[i] = layerInfo.getReplacement();
			}
		}

		final int stackSizeCount = this.offsetsPerLayer[this.offsetsPerLayer.length - 1] / Constants.BLOCK_SIZE;
		this.stackResourceHandler = new StackResourceHandler(stackSizeCount);

		startDecoding();

		// clear & return
		this.source = null;
		this.quantTables = null;
		this.stackWriter = null;

		final var tmp = this.output;
		this.output = null;
		return tmp;
	}

	private ImageRegionWriter getImageRegionWriter(TexType texType) {
		switch (texType) {
			case JPEG_TYPE_1:
				return new ChromaSubsampleStackWriter(new Type0And2PixelComposition());
			case JPEG_TYPE_2:
				return new BaseStackWriter(new Type1PixelComposition());
			case JPEG_TYPE_3:
				return new BaseStackWriter(new Type0And2PixelComposition());
			default:
				throw new IllegalArgumentException("TexType " + texType + " is not supported");
		}
	}

	private void startDecoding() {
		// TODO needs to be replaced with a proper ServiceExecutor, so it can work in a resource sensible way.

		Stream.generate(this::getNextStack) //
				.peek(this::decodeStack) //
				.limit(this.stackWriter.getNumberOfSteps()) //
				.collect(Collectors.toList()) // sequential -> parallel
				.parallelStream() //
				.peek(this::processStack) //
				.peek(s -> this.stackWriter.writeImageRegion(s, this.output)) //
				.forEach(this::returnStack);
	}

	private StackSet getNextStack() {
		final StackSet stack = this.stackResourceHandler.getFreeStack();
		stack.setId(this.lastStackId++);
		return stack;
	}

	private void returnStack(StackSet stack) {
		this.stackResourceHandler.returnStack(stack);
	}

	protected void decodeStack(StackSet stack) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (this.hasDefaultForLayer[layerIdx]) {
				copyDefaultToLayer(stack, layerIdx);
			} else {
				for (int blockIdx = 0; blockIdx < this.blocksPerLayer[layerIdx]; ++blockIdx) {
					decodeNextBlock(layerIdx);
					transferDecodeToData(layerIdx, stack.data, this.offsetsPerLayer[layerIdx] + blockIdx * Constants.BLOCK_SIZE);
				}
			}
		}
	}

	private void copyDefaultToLayer(StackSet stack, int layerIdx) {
		final int startIdx = this.offsetsPerLayer[layerIdx];
		final int endIdx = this.offsetsPerLayer[layerIdx + 1];
		stack.data[startIdx] = this.defaultForLayer[layerIdx];
		for (int dstPos = startIdx + 1, length = 1; dstPos < endIdx; dstPos += length, length += length) {
			System.arraycopy(stack.data, startIdx, stack.data, dstPos, dstPos + length > endIdx ? endIdx - dstPos : length);
		}
	}

	protected void processStack(StackSet stack) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			if (!this.hasDefaultForLayer[layerIdx]) {
				for (int blockIdx = 0; blockIdx < this.blocksPerLayer[layerIdx]; ++blockIdx) {
					final int dataOffset = this.offsetsPerLayer[layerIdx] + blockIdx * Constants.BLOCK_SIZE;
					dequantizate(layerIdx, stack.data, dataOffset);
					inverseDCT(stack.data, dataOffset);
					shiftAndClamp(layerIdx, stack.data, dataOffset);
				}
			}
		}
	}

	private void decodeNextBlock(int layerIdx) {
		final LayerType type = this.typePerLayer[layerIdx];
		final HuffmanTable dc = Constants.getHuffmanTable(type, 0);
		final HuffmanTable ac = Constants.getHuffmanTable(type, 1);
		HuffmanDecoder.decode(dc, ac, this.source, this.decoderOutput, 0, Constants.BLOCK_SIZE);
	}

	private void transferDecodeToData(int layerId, int[] data, int dataOffset) {
		for (int i = dataOffset, n = 0; i < dataOffset + Constants.BLOCK_SIZE; ++i, ++n) {
			data[i] = this.decoderOutput[Constants.ZIGZAG[n]];
		}
		final int prevDC = this.dcValues[layerId];
		final int blockDC = data[dataOffset];
		final int newDC = blockDC + prevDC;
		this.dcValues[layerId] = newDC;
		data[dataOffset] = newDC;
	}

	private void dequantizate(int layerIdx, int[] data, int dataOffset) {
		final float[] quantTable = this.quantTables[layerIdx];
		final int blockStart = dataOffset;
		final int blockEnd = blockStart + Constants.BLOCK_SIZE;

		for (int blockIdx = blockStart, quantIdx = 0; blockIdx < blockEnd; ++blockIdx, ++quantIdx) {
			data[blockIdx] = Math.round(data[blockIdx] * quantTable[quantIdx]);
		}
	}

	private void inverseDCT(int[] data, int dataOffset) {
		FastIntegerIDCT.idct(data, dataOffset);
	}

	private void shiftAndClamp(int layerIdx, int[] data, int dataOffset) {
		switch (this.typePerLayer[layerIdx]) {
			case CHROMA:
				shiftAndClamp(data, dataOffset, 0, -256, 255);
				break;
			case LUMINANCE:
				shiftAndClamp(data, dataOffset, 128, 0, 255);
				break;
			default:
				throw new IllegalArgumentException("Unknown type: " + this.typePerLayer[layerIdx]);
		}
	}

	private void shiftAndClamp(int[] data, int offset, int shift, int min, int max) {
		for (int i = offset; i < offset + Constants.BLOCK_SIZE; ++i) {
			data[i] = Math.max(min, Math.min(max, data[i] + shift));
		}
	}

}
