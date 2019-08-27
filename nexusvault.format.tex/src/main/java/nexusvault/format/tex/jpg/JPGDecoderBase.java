package nexusvault.format.tex.jpg;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureRawData;
import nexusvault.format.tex.jpg.Constants.LayerType;
import nexusvault.format.tex.struct.StructTextureFileHeader;

abstract class JPGDecoderBase {

	// private static final IDCTLookUp IDCT = new IDCTLookUp(Constants.BLOCK_HEIGHT, Constants.BLOCK_WIDTH);
	private static final HuffmanDecoder decoder = new HuffmanDecoder();

	private final int[] decoderOutput = new int[Constants.BLOCK_SIZE];
	private final int[] dcValues = new int[Constants.NUMBER_OF_LAYERS];

	private final PixelCompositionStrategy pixelCompositor;

	private LayerType[] typePerLayer;
	private float[][] quantTables;

	private StructTextureFileHeader textureHeader;
	private BitSupply source;

	protected TextureImage image;
	private int lastStackId;

	protected JPGDecoderBase(PixelCompositionStrategy pixelCompositor) {
		if (pixelCompositor == null) {
			throw new IllegalArgumentException("'pixelCompositor' must not be null");
		}
		this.pixelCompositor = pixelCompositor;
	}

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
		typePerLayer = Constants.TYPE_PER_LAYER[format];
		lastStackId = 0;
	}

	protected final void initializeData(StructTextureFileHeader header, ImageMetaInformation meta, TextureRawData data) {
		textureHeader = header;
		source = new ByteBufferBitSupplier(data.createView(meta.offset, meta.length));
	}

	protected final void initializeTextureImage(ImageMetaInformation meta) {
		final TextureImageFormat imageFormat = TextureImageFormat.ARGB;
		final byte[] imageData = new byte[meta.height * meta.width * imageFormat.getBytesPerPixel()];
		image = new TextureImage(meta.width, meta.height, imageFormat, imageData);
	}

	public TextureImageFormat getImageFormat() {
		return TextureImageFormat.ARGB;
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
		// TODO needs to be replaced with a proper ServiceExecutor, so it can work in a resource sensible way.

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
		FastIntegerIDCT.idct(data, dataOffset);
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

	protected final PixelCompositionStrategy getPixelCompositor() {
		return pixelCompositor;
	}

	abstract void loadFormatSpecificConfig(StructTextureFileHeader header, ImageMetaInformation meta);

	abstract StackSet getFreeStack();

	abstract void decodeStack(StackSet stack);

	abstract void processStack(StackSet stack);

	abstract void writeStack(StackSet stack);

	abstract void returnStack(StackSet stack);

	abstract long getNumberOfDecodableStacks();

}
