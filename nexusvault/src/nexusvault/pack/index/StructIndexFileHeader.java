package nexusvault.pack.index;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

final class StructIndexFileHeader {

	public final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIndexFileHeader.class);

	@Order(1)
	@StructField(DataType.BIT_32)
	public int signature; // 0x000

	@Order(2)
	@StructField(DataType.BIT_32)
	public int version; // 0x004

	@Order(3)
	@StructField(value = DataType.BIT_8, length = 512)
	private byte[] unknown_008; // 0x008

	@Order(4)
	@StructField(DataType.UBIT_64)
	public long fileSize; // 0x208

	@Order(5)
	@StructField(DataType.UBIT_64)
	public long unknown_210; // 0x210

	@Order(6)
	@StructField(DataType.UBIT_64)
	public long offsetPackHeaders; // 0x218

	@Order(7)
	@StructField(DataType.UBIT_64)
	public long numPackHeaders; // 0x220

	@Order(8)
	@StructField(DataType.UBIT_64)
	public long rootPackHeaderIndex; // 0x228

	@Order(9)
	@StructField(value = DataType.BIT_8, length = 16)
	private byte[] unknown_230; // 0x230

}