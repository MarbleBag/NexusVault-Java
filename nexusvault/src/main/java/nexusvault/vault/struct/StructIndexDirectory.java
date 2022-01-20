package nexusvault.vault.struct;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructIndexDirectory implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIndexDirectory.class);

	static {
		if (SIZE_IN_BYTES != 0x08) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(DataType.UBIT_32)
	public int nameOffset; // 0x000

	@Order(2)
	@StructField(DataType.UBIT_32)
	public int directoryIndex; // 0x004

	public StructIndexDirectory() {

	}

	public StructIndexDirectory(BinaryReader reader) {
		read(reader);
	}

	public StructIndexDirectory(int nameOffset, int directoryIndex) {
		this.nameOffset = nameOffset;
		this.directoryIndex = directoryIndex;
	}

	@Override
	public void read(BinaryReader reader) {
		this.nameOffset = (int) reader.readUInt32();
		this.directoryIndex = (int) reader.readUInt32();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.nameOffset);
		writer.writeInt32(this.directoryIndex);
	}

}
