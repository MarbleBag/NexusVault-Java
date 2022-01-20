package nexusvault.vault.struct;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.DataType;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructArchiveRootElement implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructArchiveRootElement.class);
	/** used for archive files */
	public static final int SIGNATURE_AARC = 'A' << 24 | 'A' << 16 | 'R' << 8 | 'C';

	static {
		if (SIZE_IN_BYTES != 0x10) {
			throw new StructException();
		}
	}

	@StructField(DataType.BIT_32)
	public int signature;

	@StructField(DataType.BIT_32)
	public int version;

	@StructField(DataType.BIT_32)
	public int entryCount;

	@StructField(DataType.BIT_32)
	public int headerIdx;

	public StructArchiveRootElement() {

	}

	public StructArchiveRootElement(int signature, int version, int entryCount, int headerIdx) {
		this.signature = signature;
		this.version = version;
		this.entryCount = entryCount;
		this.headerIdx = headerIdx;
	}

	public StructArchiveRootElement(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.signature = reader.readInt32();
		this.version = reader.readInt32();
		this.entryCount = reader.readInt32();
		this.headerIdx = reader.readInt32();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.signature);
		writer.writeInt32(this.version);
		writer.writeInt32(this.entryCount);
		writer.writeInt32(this.headerIdx);
	}
}
