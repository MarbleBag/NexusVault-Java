package nexusvault.vault.struct;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructPackFileHeader implements ReadAndWritable {

	public final static int SIGNATURE = 'P' << 24 | 'A' << 16 | 'C' << 8 | 'K';
	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructPackFileHeader.class);

	static {
		if (SIZE_IN_BYTES != 0x230) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(DataType.BIT_32)
	public int signature; // 0x000

	@Order(2)
	@StructField(DataType.BIT_32)
	public int version; // 0x004

	@Order(3)
	@StructField(value = DataType.BIT_8, length = 512)
	private final byte[] unknown_008; // 0x008

	@Order(4)
	@StructField(DataType.UBIT_64)
	public long endOfFile; // 0x208

	@Order(5)
	@StructField(DataType.UBIT_64)
	public long unknown_210; // 0x210

	@Order(6)
	@StructField(DataType.UBIT_64)
	public long indexOffset; // 0x218

	@Order(7)
	@StructField(DataType.UBIT_32)
	public long indexCount; // 0x220

	@Order(8)
	@StructField(DataType.BIT_32)
	public int unknown_224; // 0x224

	@Order(9)
	@StructField(DataType.UBIT_64)
	public long rootEntryIdx; // 0x228

	// @Order(10)
	// @StructField(value = DataType.BIT_64, length = 2)
	// private long[] blockGuardStop; // 0x230 //used for blockguards. First value needs to be 0, second value is equal to the next block.

	public StructPackFileHeader() {
		this.signature = SIGNATURE;
		this.unknown_008 = new byte[512];
	}

	public StructPackFileHeader(BinaryReader reader) {
		this();
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.signature = reader.readInt32();
		this.version = reader.readInt32();
		reader.readInt8(this.unknown_008, 0, this.unknown_008.length);
		this.endOfFile = reader.readInt64();
		this.unknown_210 = reader.readInt64();
		this.indexOffset = reader.readInt64();
		this.indexCount = reader.readUInt32();
		this.unknown_224 = reader.readInt32();
		this.rootEntryIdx = reader.readInt64();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.signature);
		writer.writeInt32(this.version);
		writer.writeInt8(this.unknown_008, 0, this.unknown_008.length);
		writer.writeInt64(this.endOfFile);
		writer.writeInt64(this.unknown_210);
		writer.writeInt64(this.indexOffset);
		writer.writeInt32(this.indexCount);
		writer.writeInt32(this.unknown_224);
		writer.writeInt64(this.rootEntryIdx);
	}

}