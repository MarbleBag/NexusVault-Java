package nexusvault.format.tbl.struct;

import static kreed.reflection.struct.DataType.UBIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import java.util.Objects;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.NotUsedForPaddingException;
import nexusvault.shared.exception.StructException;

public final class StructColumnData {

	static {
		if (StructUtil.sizeOf(StructColumnData.class) != 0x18) {
			throw new StructException();
		}
	}

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructColumnData.class) /* 24 */;

	/** number of UTF-16 encoded characters, aligned to 16 byte and null terminated */
	@Order(1)
	@StructField(UBIT_32)
	public long nameLength; // 0x00

	@Order(2)
	@StructField(UBIT_32)
	public long padding_04; // 0x04

	@Order(3)
	@StructField(UBIT_64)
	public long nameOffset; // 0x08

	@Order(4)
	@StructField(UBIT_32)
	public int dataType; // 0x10

	/**
	 * <ul>
	 * <li>int32: 24 or (very rare) 16 or 112
	 * <li>int64: 24
	 * <li>string: 104 or (very rare) 8 or 96
	 * <li>float: 24 or (very rare) 16
	 * <li>bool: 24
	 * </ul>
	 */
	@Order(6)
	@StructField(UBIT_32)
	public long unk2; // 0x14

	public StructColumnData() {

	}

	public StructColumnData(BinaryReader reader) {
		final long headerStart = reader.getPosition();
		final long headerEnd = headerStart + SIZE_IN_BYTES;

		this.nameLength = reader.readUInt32(); // o:4
		this.padding_04 = reader.readUInt32(); // o:8
		this.nameOffset = reader.readInt64(); // o:16
		this.dataType = reader.readInt32(); // o:18
		this.unk2 = reader.readUInt32(); // o:24

		if (this.padding_04 != 0) {
			throw new NotUsedForPaddingException("padding_04");
		}
		if (reader.getPosition() != headerEnd) {
			throw new StructException("Expected number of bytes " + SIZE_IN_BYTES + " read bytes: " + (reader.getPosition() - headerStart));
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructColumnData [nameLength=");
		builder.append(this.nameLength);
		builder.append(", unk1=");
		builder.append(this.padding_04);
		builder.append(", nameOffset=");
		builder.append(this.nameOffset);
		builder.append(", dataType=");
		builder.append(this.dataType);
		builder.append(", unk2=");
		builder.append(this.unk2);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.dataType, this.nameLength, this.nameOffset, this.padding_04, this.unk2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StructColumnData other = (StructColumnData) obj;
		return this.dataType == other.dataType && this.nameLength == other.nameLength && this.nameOffset == other.nameOffset
				&& this.padding_04 == other.padding_04 && this.unk2 == other.unk2;
	}

	public DataType getDataType() {
		return DataType.resolve(this.dataType);
	}

}
