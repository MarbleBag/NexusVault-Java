package nexusvault.pack.index;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

final class StructIdxDirectoryHeader {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIdxDirectoryHeader.class);

	@Order(1)
	@StructField(DataType.UBIT_32)
	public long nameOffset; // 0x000

	@Order(2)
	@StructField(DataType.UBIT_32)
	public int directoryHeaderIdx; // 0x004

}
