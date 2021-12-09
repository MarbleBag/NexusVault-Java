package nexusvault.format.tex.jpg.tool;

import nexusvault.format.tex.TexType;
import nexusvault.format.tex.jpg.huffman.HuffmanTable;

public final class Constants {
	// uint8
	private final static int[] HUFF_DC_LUMA_BITS = { 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 };
	private final static int[] HUFF_DC_LUMA_VALS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
	private final static int[] HUFF_AC_LUMA_BITS = { 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 125 };
	private final static int[] HUFF_AC_LUMA_VALS = { 0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71,
			0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17, 0x18,
			0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53,
			0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x83,
			0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9,
			0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6,
			0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa };

	// uint8
	private final static int[] HUFF_DC_CHROMA_BITS = { 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 };
	private final static int[] HUFF_DC_CHROMA_VALS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
	private final static int[] HUFF_AC_CHROMA_BITS = { 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 119 };
	private final static int[] HUFF_AC_CHROMA_VALS = { 0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13,
			0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1,
			0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49,
			0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
			0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6,
			0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3,
			0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9,
			0xfa };

	private final static int[] QUANT_TABLE_LUMA = { 16, 11, 10, 16, 24, 40, 51, 61, 12, 12, 14, 19, 26, 58, 60, 55, 14, 13, 16, 24, 40, 57, 69, 56, 14, 17, 22,
			29, 51, 87, 80, 62, 18, 22, 37, 56, 68, 109, 103, 77, 24, 35, 55, 64, 81, 104, 113, 92, 49, 64, 78, 87, 103, 121, 120, 101, 72, 92, 95, 98, 112,
			100, 103, 99 };

	private final static int[] QUANT_TABLE_CHROMA = { 17, 18, 24, 47, 99, 99, 99, 99, 18, 21, 26, 66, 99, 99, 99, 99, 24, 26, 56, 99, 99, 99, 99, 99, 47, 66,
			99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
			99 };

	public final static int BLOCK_WIDTH = 8;
	public final static int BLOCK_HEIGHT = 8;
	public final static int BLOCK_SIZE = BLOCK_WIDTH * BLOCK_HEIGHT;

	public final static int NUMBER_OF_LAYERS = 4;

	public final static int[][] BLOCKS_PER_LAYER = { { 4, 1, 1, 4 }, { 1, 1, 1, 1 }, { 1, 1, 1, 1 } };
	public final static int[] MAX_BLOCKS_PER_LAYER = { 4, 1, 1 };
	public final static boolean[][] LAYER_IS_SUBSAMPLED = { { false, true, true, false }, { false, false, false, false }, { false, false, false, false } };

	public final static SignalType[][] TYPE_PER_LAYER = { //
			{ SignalType.LUMINANCE, SignalType.CHROMINANCE, SignalType.CHROMINANCE, SignalType.LUMINANCE }, //
			{ SignalType.LUMINANCE, SignalType.LUMINANCE, SignalType.LUMINANCE, SignalType.LUMINANCE }, //
			{ SignalType.LUMINANCE, SignalType.CHROMINANCE, SignalType.CHROMINANCE, SignalType.LUMINANCE } };

	public final static boolean[][] LAYER_WITH_POSSIBLE_DEFAULT_VALUES = { //
			{ false, false, false, false }, //
			{ false, false, true, true }, //
			{ false, false, false, true } };

	public final static int[][] OFFSETS_PER_LAYER = {
			{ 0, 4 * Constants.BLOCK_SIZE, 5 * Constants.BLOCK_SIZE, 6 * Constants.BLOCK_SIZE, 10 * Constants.BLOCK_SIZE },
			{ 0, 1 * Constants.BLOCK_SIZE, 2 * Constants.BLOCK_SIZE, 3 * Constants.BLOCK_SIZE, 4 * Constants.BLOCK_SIZE },
			{ 0, 1 * Constants.BLOCK_SIZE, 2 * Constants.BLOCK_SIZE, 3 * Constants.BLOCK_SIZE, 4 * Constants.BLOCK_SIZE } };

	public final static int[][] OFFSETS_PER_LAYER2 = {
			{ 0, 4 * Constants.BLOCK_SIZE, 8 * Constants.BLOCK_SIZE, 12 * Constants.BLOCK_SIZE, 16 * Constants.BLOCK_SIZE },
			{ 0, 1 * Constants.BLOCK_SIZE, 2 * Constants.BLOCK_SIZE, 3 * Constants.BLOCK_SIZE, 4 * Constants.BLOCK_SIZE },
			{ 0, 1 * Constants.BLOCK_SIZE, 2 * Constants.BLOCK_SIZE, 3 * Constants.BLOCK_SIZE, 4 * Constants.BLOCK_SIZE } };

	public final static int[] ZIGZAG = ZigZagPatternBuilder.calculateRowFirstZigZagIndices(BLOCK_WIDTH, BLOCK_HEIGHT);

	private final static HuffmanTable CHROMA_DC_HUFFMAN = new HuffmanTable(HUFF_DC_CHROMA_BITS, HUFF_DC_CHROMA_VALS);
	private final static HuffmanTable CHROMA_AC_HUFFMAN = new HuffmanTable(HUFF_AC_CHROMA_BITS, HUFF_AC_CHROMA_VALS);
	private final static HuffmanTable LUMA_DC_HUFFMAN = new HuffmanTable(HUFF_DC_LUMA_BITS, HUFF_DC_LUMA_VALS);
	private final static HuffmanTable LUMA_AC_HUFFMAN = new HuffmanTable(HUFF_AC_LUMA_BITS, HUFF_AC_LUMA_VALS);

	private final static HuffmanTable[][] HUFFMAN_TABLES;

	public enum SignalType {
		LUMINANCE,
		CHROMINANCE
	}

	static {
		HUFFMAN_TABLES = new HuffmanTable[2][2];
		HUFFMAN_TABLES[SignalType.LUMINANCE.ordinal()][0] = LUMA_DC_HUFFMAN;
		HUFFMAN_TABLES[SignalType.LUMINANCE.ordinal()][1] = LUMA_AC_HUFFMAN;
		HUFFMAN_TABLES[SignalType.CHROMINANCE.ordinal()][0] = LUMA_DC_HUFFMAN;
		HUFFMAN_TABLES[SignalType.CHROMINANCE.ordinal()][1] = CHROMA_AC_HUFFMAN;
	}

	public static HuffmanTable getHuffmanTable(SignalType type, int clazz) {
		return HUFFMAN_TABLES[type.ordinal()][clazz];
	}

	public static int[] getQuantTable(SignalType type) {
		switch (type) {
			case CHROMINANCE:
				return QUANT_TABLE_CHROMA;
			case LUMINANCE:
				return QUANT_TABLE_LUMA;
			default:
				throw new UnsupportedOperationException();
		}
	}

	public static float[] adjustQuantTable(int[] quantTable, int factor) {
		final float cof = (200 - factor * 2) * 0.01f;
		final float[] adjustedQuantTable = new float[quantTable.length];
		for (int i = 0; i < adjustedQuantTable.length; ++i) {
			final float val = quantTable[i] * cof;
			adjustedQuantTable[i] = Math.max(1f, Math.min(val, 255f));
		}
		return adjustedQuantTable;
	}

	public static float[] getAdjustedQuantTable(SignalType type, int factor) {
		return adjustQuantTable(Constants.getQuantTable(type), factor);
	}

	public static int getArrayIndexForType(TexType texType) {
		switch (texType) {
			case JPG1:
				return 0;
			case JPG2:
				return 1;
			case JPG3:
				return 2;
			default:
				throw new IllegalArgumentException("TexType " + texType + " is not supported");
		}
	}

}
