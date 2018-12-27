package nexusvault.format.tbl;

import static kreed.reflection.struct.DataType.BIT_16;
import static kreed.reflection.struct.DataType.UBIT_16;
import static kreed.reflection.struct.DataType.UBIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.NotUsedForPaddingException;

class StructTableFieldHeader {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructTableFieldHeader.class) /* 24 */;

	/** uint32, # of UTF-16 encoded characters, 0 terminated */
	@StructField(UBIT_32)
	public long nameLength;

	/** uint 32 */
	@StructField(UBIT_32)
	public long unk1;

	/** uint 64 */
	@StructField(UBIT_64)
	public long nameOffset;

	/** uint 16 */
	@StructField(UBIT_16)
	public int dataType;

	/** uint 16 */
	@StructField(BIT_16)
	private int padding1;

	/** uint 32 */
	@StructField(UBIT_32)
	public long unk2;

	/* post header data */

	public String name;

	public StructTableFieldHeader() {

	}

	public StructTableFieldHeader(BinaryReader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("'reader' must not be null");
		}

		final long headerStart = reader.getPosition();
		final long headerEnd = headerStart + SIZE_IN_BYTES;

		this.nameLength = reader.readUInt32(); // o:4
		this.unk1 = reader.readUInt32(); // o:8
		this.nameOffset = reader.readInt64(); // o:16
		this.dataType = reader.readInt16(); // o:18
		this.padding1 = reader.readInt16(); // o:20
		this.unk2 = reader.readUInt32(); // o:24

		if (this.padding1 != 0) {
			throw new NotUsedForPaddingException(this.getClass().getSimpleName());
		}

		if (reader.getPosition() != headerEnd) {
			throw new IllegalStateException("Expected number of bytes " + SIZE_IN_BYTES + " read bytes: " + (reader.getPosition() - headerStart));
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TableFieldHeader [nameLength=");
		builder.append(nameLength);
		builder.append(", unk1=");
		builder.append(unk1);
		builder.append(", nameOffset=");
		builder.append(nameOffset);
		builder.append(", dataType=");
		builder.append(dataType);
		builder.append(", unk2=");
		builder.append(padding1);
		builder.append(", unk3=");
		builder.append(unk2);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + dataType;
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + (int) (nameLength ^ (nameLength >>> 32));
		result = (prime * result) + (int) (nameOffset ^ (nameOffset >>> 32));
		result = (prime * result) + (int) (unk1 ^ (unk1 >>> 32));
		result = (prime * result) + (int) (unk2 ^ (unk2 >>> 32));
		return result;
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
		final StructTableFieldHeader other = (StructTableFieldHeader) obj;
		if (dataType != other.dataType) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (nameLength != other.nameLength) {
			return false;
		}
		if (nameOffset != other.nameOffset) {
			return false;
		}
		if (unk1 != other.unk1) {
			return false;
		}
		if (unk2 != other.unk2) {
			return false;
		}
		return true;
	}

	public String getFieldName() {
		return this.name;
	}

	public FieldDataType getFieldDataType() {
		return FieldDataType.resolve(dataType);
	}

}
