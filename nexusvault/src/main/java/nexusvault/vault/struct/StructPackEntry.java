package nexusvault.vault.struct;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructPackEntry implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructPackEntry.class);

	static {
		if (SIZE_IN_BYTES != 0x10) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(DataType.UBIT_64)
	public long offset;

	@Order(2)
	@StructField(DataType.UBIT_64)
	public long size;

	public StructPackEntry() {

	}

	public StructPackEntry(long offset, long size) {
		this.offset = offset;
		this.size = size;
	}

	public StructPackEntry(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.offset = reader.readInt64();
		this.size = reader.readInt64();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt64(this.offset);
		writer.writeInt64(this.size);
	}

}