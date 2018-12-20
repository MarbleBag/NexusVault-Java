package nexusvault.format.tex.jpg;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.StructTextureFileHeader;
import nexusvault.format.tex.TextureChannel;
import nexusvault.format.tex.TextureChannelType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureRawData;
import nexusvault.format.tex.jpg.Constants.LayerType;

abstract class JPGDecoderBase {

	private static final IDCTLookUp IDCT = new IDCTLookUp(Constants.BLOCK_HEIGHT, Constants.BLOCK_WIDTH);
	private static final HuffmanDecoder decoder = new HuffmanDecoder();

	private final int[] decoderOutput = new int[Constants.BLOCK_SIZE];
	private final int[] dcValues = new int[Constants.NUMBER_OF_LAYERS];

	private LayerType[] typePerLayer;
	private float[][] quantTables;

	private StructTextureFileHeader textureHeader;
	private BitSupply source;

	private PixelCalculator pixelCalculator;

	protected TextureImage image;
	private int lastStackId;

	public final TextureImage decodeImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta) {
		initializeData(header, meta, data);
		initializeConstants(header);
		loadFormatSpecificConfig(header, meta);

		initializeTextureImage(meta);
		initializeQuantTables();
		initializeDCValues();

		startDecoding();

		return returnDecodingResult();
	}

	private void initializeConstants(StructTextureFileHeader header) {
		final int format = header.compressionFormat;
		this.typePerLayer = Constants.TYPE_PER_LAYER[format];

		switch (format) {
			case 0:
			case 2:
				pixelCalculator = new Type0And2PixelCalculator();
				break;
			case 1:
				pixelCalculator = new Type1PixelCalculator();
				break;
		}

		this.lastStackId = 0;
	}

	protected final void initializeData(StructTextureFileHeader header, ImageMetaInformation meta, TextureRawData data) {
		textureHeader = header;
		source = new ByteBufferBitSupplier(data.createView(meta.offset, meta.length));
	}

	protected final void initializeTextureImage(ImageMetaInformation meta) {
		final List<TextureChannelType> types = pixelCalculator.getTextureChannelTypes();
		final TextureChannel[] channels = types.stream().map(type -> new TextureChannel(type, new byte[meta.height * meta.width * type.getBytesPerPixel()]))
				.toArray(TextureChannel[]::new);
		image = new TextureImage(meta.width, meta.height, channels);
	}

	protected final void initializeQuantTables() {
		quantTables = new float[Constants.NUMBER_OF_LAYERS][];
		for (int i = 0; i < quantTables.length; ++i) {
			quantTables[i] = Constants.getAdjustedDefaultQuantTable(typePerLayer[i], getLayerQualityFactor(i));
		}
	}

	protected final void initializeDCValues() {
		for (int i = 0; i < dcValues.length; ++i) {
			dcValues[i] = 0;
		}
	}

	private final void startDecoding() {
		final List<StackSet> intermediate = Stream.generate(this::getNextStack).peek(this::decodeStack).limit(getNumberOfDecodableStacks())
				.collect(Collectors.toList());
		intermediate.parallelStream().peek(this::processStack).peek(this::writeStack).forEach(this::returnStack);
	}

	private final StackSet getNextStack() { // TODO
		final StackSet stack = getFreeStack();
		stack.setId(lastStackId++);
		return stack;
	}

	protected final TextureImage returnDecodingResult() {
		final TextureImage result = image;
		image = null;
		return result;
	}

	protected final void decodeNextBlock(LayerType type) {
		final HuffmanTable dc = Constants.getHuffmanTable(type, 0);
		final HuffmanTable ac = Constants.getHuffmanTable(type, 1);
		decoder.decode(dc, ac, source, decoderOutput, 0, Constants.BLOCK_SIZE);
	}

	protected final void transferDecodeToData(int layerId, int[] data, int dataOffset) {
		for (int i = dataOffset, n = 0; i < (dataOffset + Constants.BLOCK_SIZE); ++i, ++n) {
			data[i] = decoderOutput[Constants.ZIGZAG[n]];
		}
		final int prevDC = dcValues[layerId];
		final int blockDC = data[dataOffset];
		final int newDC = blockDC + prevDC;
		dcValues[layerId] = newDC;
		data[dataOffset] = newDC;
	}

	protected void dequantizate(int layerId, int[] data, int dataOffset) {
		final float[] quantTable = getQuantTableForLayer(layerId);
		final int blockStart = dataOffset;
		final int blockEnd = blockStart + Constants.BLOCK_SIZE;

		for (int blockIdx = blockStart, quantIdx = 0; blockIdx < blockEnd; ++blockIdx, ++quantIdx) {
			data[blockIdx] = Math.round(data[blockIdx] * quantTable[quantIdx]);
		}
	}

	protected final void inverseDCT(int[] data, int dataOffset, int[] inverseDCTBuffer) {
		for (int y0 = 0; y0 < Constants.BLOCK_HEIGHT; ++y0) {
			for (int x0 = 0; x0 < Constants.BLOCK_WIDTH; ++x0) {
				final double value = IDCT.IDCT(x0, y0, data, dataOffset, Constants.BLOCK_WIDTH, Constants.BLOCK_HEIGHT);
				inverseDCTBuffer[x0 + (y0 * Constants.BLOCK_WIDTH)] = (int) Math.round(value);
			}
		}
		System.arraycopy(inverseDCTBuffer, 0, data, dataOffset, Constants.BLOCK_SIZE);
	}

	protected void shiftAndClamp(int layerId, int[] data, int dataOffset) {
		switch (getLayerType(layerId)) {
			case CHROMA:
				shiftAndClamp(data, dataOffset, 0, -256, 255);
				break;
			case LUMINANCE:
				shiftAndClamp(data, dataOffset, 128, 0, 255);
				break;
			default:
				throw new IllegalArgumentException("Unknown type: " + getLayerType(layerId));
		}
	}

	protected final void shiftAndClamp(int[] data, int offset, int shift, int min, int max) {
		for (int i = offset; i < (offset + Constants.BLOCK_SIZE); ++i) {
			data[i] = Math.max(min, Math.min(max, data[i] + shift));
		}
	}

	protected final float[] getQuantTableForLayer(int layerId) {
		return quantTables[layerId];
	}

	protected final LayerType getLayerType(int layerId) {
		return typePerLayer[layerId];
	}

	protected final boolean hasLayerDefaultValue(int layerId) {
		return textureHeader.getLayer(layerId).hasReplacement();
	}

	protected final byte getLayerDefaultValue(int layerId) {
		return textureHeader.getLayer(layerId).getReplacement();
	}

	protected final byte getLayerQualityFactor(int layerId) {
		return textureHeader.getLayer(layerId).getQuality();
	}

	protected final PixelCalculator getPixelCalculator() {
		return pixelCalculator;
	}

	abstract void loadFormatSpecificConfig(StructTextureFileHeader header, ImageMetaInformation meta);

	abstract StackSet getFreeStack();

	abstract void decodeStack(StackSet stack);

	abstract void processStack(StackSet stack);

	abstract void writeStack(StackSet stack);

	abstract void returnStack(StackSet stack);

	abstract long getNumberOfDecodableStacks();

}
