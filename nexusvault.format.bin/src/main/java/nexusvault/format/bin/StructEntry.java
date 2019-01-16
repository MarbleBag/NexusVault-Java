package nexusvault.format.bin;

import static kreed.reflection.struct.DataType.BIT_32;

import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

class StructEntry {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructEntry.class);

	@StructField(BIT_32)
	public int id;

	/**
	 * Offset starts at {@link StructFileHeader#textOffset}. <br>
	 * Each character is with 2 bytes encoded, hence, to compute the correct start position, this value needs to be multiplied with 2.
	 */
	@StructField(BIT_32)
	public int characterOffset;
}
