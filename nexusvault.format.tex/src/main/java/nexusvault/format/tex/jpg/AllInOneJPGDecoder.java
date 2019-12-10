package nexusvault.format.tex.jpg;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.jpg.tool.Constants;
import nexusvault.format.tex.jpg.tool.Constants.LayerType;
import nexusvault.format.tex.jpg.tool.FastIntegerIDCT;
import nexusvault.format.tex.jpg.tool.HuffmanTable;
import nexusvault.format.tex.jpg.tool.StackSet;
import nexusvault.format.tex.jpg.tool.decoder.BinaryReaderBitSupplier;
import nexusvault.format.tex.jpg.tool.decoder.BitSupply;
import nexusvault.format.tex.jpg.tool.decoder.HuffmanDecoder;
import nexusvault.format.tex.struct.StructTextureFileHeader;

final class AllInOneJPGDecoder implements JPGDecoder {

	private static final HuffmanDecoder decoder = new HuffmanDecoder();

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

	@Override
	public final byte[] decode(StructTextureFileHeader header, BinaryReader data, int byteLength, int width, int height) {
		final var texType = TexType.resolve(header);

		final int constantIdx = Constants.getArrayIndexForType(texType);
		typePerLayer = Constants.TYPE_PER_LAYER[constantIdx];
		blocksPerLayer = Constants.BLOCKS_PER_LAYER[constantIdx];
		offsetsPerLayer = Constants.OFFSETS_PER_LAYER[constantIdx];
		quantTables = new float[Constants.NUMBER_OF_LAYERS][];

		stackWriter = getImageRegionWriter(texType);
		stackWriter.setImageSize(width, height);
		source = new BinaryReaderBitSupplier(data, byteLength);
		output = new byte[width * height * TextureImageFormat.ARGB.getBytesPerPixel()];
		lastStackId = 0;

		for (int i = 0; i < Constants.NUMBER_OF_LAYERS; ++i) {
			final var layerInfo = header.layerInfos[i];
			dcValues[i] = 0;
			quantTables[i] = Constants.getAdjustedQuantTable(typePerLayer[i], layerInfo.getQuality());
			hasDefaultForLayer[i] = layerInfo.hasReplacement();
			if (hasDefaultForLayer[i]) {
				defaultForLayer[i] = layerInfo.getReplacement();
			}
		}

		final int stackSizeCount = offsetsPerLayer[offsetsPerLayer.length - 1] / Constants.BLOCK_SIZE;
		stackResourceHandler = new StackResourceHandler(stackSizeCount);

		startDecoding();

		// clear & return
		source = null;
		quantTables = null;
		stackWriter = null;

		final var tmp = output;
		output = null;
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

		final List<StackSet> intermediate = Stream.generate(this::getNextStack).peek(this::decodeStack).limit(stackWriter.getNumberOfSteps())
				.collect(Collectors.toList());

		intermediate.parallelStream().peek(this::processStack).peek(s -> stackWriter.writeImageRegion(s, output)).forEach(this::returnStack);
	}

	private StackSet getNextStack() {
		final StackSet stack = stackResourceHandler.getFreeStack();
		stack.setId(lastStackId++);
		return stack;
	}

	private void returnStack(StackSet stack) {
		stackResourceHandler.returnStack(stack);
	}

	protected void decodeStack(StackSet stack) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			for (int blockIdx = 0; blockIdx < blocksPerLayer[layerIdx]; ++blockIdx) {
				decodeNextBlock(layerIdx);
				transferDecodeToData(layerIdx, stack.data, offsetsPerLayer[layerIdx] + (blockIdx * Constants.BLOCK_SIZE));
			}
		}
	}

	protected void processStack(StackSet stack) {
		for (int layerIdx = 0; layerIdx < Constants.NUMBER_OF_LAYERS; ++layerIdx) {
			for (int blockIdx = 0; blockIdx < blocksPerLayer[layerIdx]; ++blockIdx) {
				final int dataOffset = offsetsPerLayer[layerIdx] + (blockIdx * Constants.BLOCK_SIZE);
				dequantizate(layerIdx, stack.data, dataOffset);
				inverseDCT(stack.data, dataOffset);
				shiftAndClamp(layerIdx, stack.data, dataOffset);
			}
		}
	}

	private void decodeNextBlock(int layerIdx) {
		final LayerType type = typePerLayer[layerIdx];
		final HuffmanTable dc = Constants.getHuffmanTable(type, 0);
		final HuffmanTable ac = Constants.getHuffmanTable(type, 1);
		decoder.decode(dc, ac, source, decoderOutput, 0, Constants.BLOCK_SIZE);
	}

	private void transferDecodeToData(int layerId, int[] data, int dataOffset) {
		for (int i = dataOffset, n = 0; i < (dataOffset + Constants.BLOCK_SIZE); ++i, ++n) {
			data[i] = decoderOutput[Constants.ZIGZAG[n]];
		}
		final int prevDC = dcValues[layerId];
		final int blockDC = data[dataOffset];
		final int newDC = blockDC + prevDC;
		dcValues[layerId] = newDC;
		data[dataOffset] = newDC;
	}

	private void dequantizate(int layerIdx, int[] data, int dataOffset) {
		final float[] quantTable = quantTables[layerIdx];
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
		switch (typePerLayer[layerIdx]) {
			case CHROMA:
				shiftAndClamp(data, dataOffset, 0, -256, 255);
				break;
			case LUMINANCE:
				shiftAndClamp(data, dataOffset, 128, 0, 255);
				break;
			default:
				throw new IllegalArgumentException("Unknown type: " + typePerLayer[layerIdx]);
		}
	}

	private void shiftAndClamp(int[] data, int offset, int shift, int min, int max) {
		for (int i = offset; i < (offset + Constants.BLOCK_SIZE); ++i) {
			data[i] = Math.max(min, Math.min(max, data[i] + shift));
		}
	}

}
