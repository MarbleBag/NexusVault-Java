package nexusvault.format.bin.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class StructFileHeader {
	public static void main(String[] arg) {
		System.out.println(StructUtil.analyzeStruct(StructFileHeader.class, true));
	}

	public static final int SIGNATURE = 'L' << 24 | 'T' << 16 | 'E' << 8 | 'X';

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructFileHeader.class);

	@Order(1)
	@StructField(BIT_32)
	public int signature;

	@Order(2)
	@StructField(BIT_32)
	public int version;

	/**
	 * Values are hardcoded
	 * <ul>
	 * <li>1 - english
	 * <li>2 - german
	 * <li>3 - french
	 * <li>4 - korean
	 * </ul>
	 */
	@Order(3)
	@StructField(UBIT_64)
	public long languageType;

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(4)
	@StructField(UBIT_64)
	public long languageTagNameLength;

	@Order(5)
	@StructField(UBIT_64)
	public long languageTagNameOffset;

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(6)
	@StructField(UBIT_64)
	public long languageShortNameLength;

	@Order(7)
	@StructField(UBIT_64)
	public long languageShortNameOffset;

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(8)
	@StructField(UBIT_64)
	public long languageLongNameLength;

	@Order(9)
	@StructField(UBIT_64)
	public long languageLongtNameOffset;

	@Order(10)
	@StructField(UBIT_64)
	public long entryCount;

	@Order(11)
	@StructField(UBIT_64)
	public long entryOffset;

	/**
	 * number of characters, each is UTF16 encoded
	 */
	@Order(12)
	@StructField(UBIT_64)
	public long totalTextSize;

	@Order(13)
	@StructField(UBIT_64)
	public long textOffset;

}
