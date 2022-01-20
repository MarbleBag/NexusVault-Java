package nexusvault.shared;

import static kreed.reflection.struct.DataType.BIT_32;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class FileSignatureAndVersion implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(FileSignatureAndVersion.class);

	static {
		if (SIZE_IN_BYTES != 0x08) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(BIT_32)
	public int signature; // 0x00

	@Order(2)
	@StructField(BIT_32)
	public int version; // 0x04

	public FileSignatureAndVersion() {

	}

	public FileSignatureAndVersion(int signature, int version) {
		this.signature = signature;
		this.version = version;
	}

	@Override
	public void read(BinaryReader reader) {
		this.signature = reader.readInt32();
		this.version = reader.readInt32();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.signature);
		writer.writeInt32(this.version);
	}

}
