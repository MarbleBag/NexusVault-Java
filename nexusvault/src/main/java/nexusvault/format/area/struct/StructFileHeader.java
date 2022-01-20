package nexusvault.format.area.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public class StructFileHeader {

	public static final int SIGNATURE = 'A' << 24 | 'R' << 16 | 'E' << 8 | 'A';
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructFileHeader.class);

	@Order(1)
	@StructField(DataType.BIT_32)
	public int signature; // 0x000

	@Order(2)
	@StructField(DataType.BIT_32)
	public int version; // 0x004

}
