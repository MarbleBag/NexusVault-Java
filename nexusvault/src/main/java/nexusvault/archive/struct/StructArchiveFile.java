package nexusvault.archive.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructArchiveFile {

	static {
		if (StructUtil.sizeOf(StructArchiveFile.class) != 0x230) {
			throw new StructException();
		}
	}

	public final static int FILE_SIGNATURE = 'P' << 24 | 'A' << 16 | 'C' << 8 | 'K';

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructArchiveFile.class);

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
	public long packOffset; // 0x218

	@Order(7)
	@StructField(DataType.UBIT_32)
	public long packCount; // 0x220

	@Order(8)
	@StructField(DataType.BIT_32)
	public int unk_224; // 0x224

	@Order(9)
	@StructField(DataType.UBIT_64)
	public long packRootIdx; // 0x228

	// @Order(10)
	// @StructField(value = DataType.BIT_64, length = 2)
	// private long[] blockGuardStop; // 0x230 //used for blockguards. First value needs to be 0, second value is equal to the next block.

}