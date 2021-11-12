package nexusvault.format.bin.struct;

import static kreed.reflection.struct.DataType.BIT_32;

import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructEntry {

	static {
		if (StructUtil.sizeOf(StructEntry.class) != 0x08) {
			throw new StructException();
		}
	}

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructEntry.class);

	@Order(1)
	@StructField(BIT_32)
	public int id; // 0x0

	/**
	 * Offset starts at {@link StructFileHeader#textOffset}. <br>
	 * Each character is UTF16 encoded, hence, to compute the correct start position, this value needs to be multiplied with 2.
	 */
	@Order(2)
	@StructField(BIT_32)
	public int characterOffset; // 0x4

	public StructEntry() {

	}

	public StructEntry(int id, int characterOffset) {
		this.id = id;
		this.characterOffset = characterOffset;
	}

}
