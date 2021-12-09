package nexusvault.format.bin.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructFileHeader {

	static {
		if (StructUtil.sizeOf(StructFileHeader.class) != 0x60) {
			throw new StructException();
		}
	}

	public static final int SIGNATURE = 'L' << 24 | 'T' << 16 | 'E' << 8 | 'X';

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructFileHeader.class);

	@Order(1)
	@StructField(BIT_32)
	public int signature; // 0x00

	@Order(2)
	@StructField(BIT_32)
	public int version; // 0x04

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
	public long languageType; // 0x08

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(4)
	@StructField(UBIT_64)
	public long languageTagNameLength; // 0x10

	@Order(5)
	@StructField(UBIT_64)
	public long languageTagNameOffset; // 0x18

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(6)
	@StructField(UBIT_64)
	public long languageShortNameLength; // 0x20

	@Order(7)
	@StructField(UBIT_64)
	public long languageShortNameOffset; // 0x28

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(8)
	@StructField(UBIT_64)
	public long languageLongNameLength; // 0x30

	@Order(9)
	@StructField(UBIT_64)
	public long languageLongtNameOffset; // 0x38

	@Order(10)
	@StructField(UBIT_64)
	public long entryCount; // 0x40

	@Order(11)
	@StructField(UBIT_64)
	public long entryOffset; // 0x48

	/**
	 * number of characters, each is UTF16 encoded
	 */
	@Order(12)
	@StructField(UBIT_64)
	public long totalTextSize; // 0x50

	@Order(13)
	@StructField(UBIT_64)
	public long textOffset; // 0x58

}
