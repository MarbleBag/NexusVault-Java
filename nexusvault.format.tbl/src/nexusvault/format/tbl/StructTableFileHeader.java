package nexusvault.format.tbl;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.BIT_64;
import static kreed.reflection.struct.DataType.UBIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.NotUsedForPaddingException;
import nexusvault.shared.exception.SignatureMismatchException;

class StructTableFileHeader {

	public static final int SIGNATURE = ('D' << 24) | ('T' << 16) | ('B' << 8) | 'L';

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructTableFileHeader.class) /* 96 */;

	/** int32; 'DTBL' */
	@StructField(BIT_32)
	public int signature; // 0x000

	/** uint32; */
	@StructField(BIT_32)
	public long version; // 0x004

	/** uint32; number of UTF-16 encoded characters, 0 terminated */
	@StructField(UBIT_32)
	public long tableNameLength; // 0x008

	/** uint32; */
	@StructField(BIT_32)
	private int padding1; // 0x00C

	/** uint64; */
	@StructField(UBIT_64)
	public long unk1; // 0x010

	/** uint32; Size of one record in bytes */
	@StructField(UBIT_32)
	public long recordSize; // 0x018

	/** uint32; */
	@StructField(BIT_32)
	private int padding2; // 0x01C

	/** uint64; number of fields */
	@StructField(UBIT_64)
	public long fieldCount; // 0x020

	/** uint64; Start offset for the first field */
	@StructField(UBIT_64)
	public long fieldOffset; // 0x028

	/** uint32; number of records */
	@StructField(UBIT_32)
	public long recordCount; // 0x030

	/** uint32; */
	@StructField(BIT_32)
	private int padding3; // 0x034

	/** uint64; Size of all records in bytes */
	@StructField(UBIT_64)
	public long totalRecordSize; // 0x038

	/** uint64; Start offset for the first record */
	@StructField(UBIT_64)
	public long recordOffset; // 0x040

	/** uint64; */
	@StructField(UBIT_64)
	public long maxId; // 0x048

	/** uint64; */
	@StructField(UBIT_64)
	public long lookupOffset; // 0x050

	/** uint64; */
	@StructField(BIT_64)
	private long padding4; // 0x058

	/* post header data */

	public String name;

	public StructTableFileHeader() {
	}

	public StructTableFileHeader(BinaryReader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("'reader' must not be null");
		}

		final long headerStart = reader.getPosition();
		final long headerEnd = headerStart + SIZE_IN_BYTES;

		this.signature = reader.readInt32(); // o:4
		if (signature != SIGNATURE) {
			throw new SignatureMismatchException("Table Header", SIGNATURE, signature);
		}

		this.version = reader.readUInt32(); // o:8
		this.tableNameLength = reader.readUInt32(); // o:12
		this.padding1 = reader.readInt32(); // o:16
		this.unk1 = reader.readInt64(); // o:24
		this.recordSize = reader.readUInt32(); // o:28
		this.padding2 = reader.readInt32(); // o:32
		this.fieldCount = reader.readInt64(); // o:40
		this.fieldOffset = reader.readInt64(); // o:48
		this.recordCount = reader.readUInt32(); // o:52
		this.padding3 = reader.readInt32(); // o:56
		this.totalRecordSize = reader.readInt64(); // o:64
		this.recordOffset = reader.readInt64(); // o:72
		this.maxId = reader.readInt64(); // o:80
		this.lookupOffset = reader.readInt64(); // o:88
		this.padding4 = reader.readInt64(); // o:96

		if (this.padding1 != 0) {
			throw new NotUsedForPaddingException(this.getClass().getSimpleName());
		}
		if (this.padding2 != 0) {
			throw new NotUsedForPaddingException(this.getClass().getSimpleName());
		}
		if (this.padding3 != 0) {
			throw new NotUsedForPaddingException(this.getClass().getSimpleName());
		}
		if (this.padding4 != 0) {
			throw new NotUsedForPaddingException(this.getClass().getSimpleName());
		}

		if (reader.getPosition() != headerEnd) {
			throw new IllegalStateException("Expected number of bytes " + SIZE_IN_BYTES + " read bytes: " + (reader.getPosition() - headerStart));
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TableFileHeader [signature=");
		builder.append(signature);
		builder.append(", version=");
		builder.append(version);
		builder.append(", tableNameLength=");
		builder.append(tableNameLength);
		builder.append(", unk1=");
		builder.append(unk1);
		builder.append(", recordSize=");
		builder.append(recordSize);
		builder.append(", fieldCount=");
		builder.append(fieldCount);
		builder.append(", fieldOffset=");
		builder.append(fieldOffset);
		builder.append(", recordCount=");
		builder.append(recordCount);
		builder.append(", totalRecordSize=");
		builder.append(totalRecordSize);
		builder.append(", recordOffset=");
		builder.append(recordOffset);
		builder.append(", maxId=");
		builder.append(maxId);
		builder.append(", lookupOffset=");
		builder.append(lookupOffset);
		builder.append(", unk2=");
		builder.append(padding2);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (fieldCount ^ (fieldCount >>> 32));
		result = (prime * result) + (int) (fieldOffset ^ (fieldOffset >>> 32));
		result = (prime * result) + (int) (lookupOffset ^ (lookupOffset >>> 32));
		result = (prime * result) + (int) (maxId ^ (maxId >>> 32));
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + (int) (recordCount ^ (recordCount >>> 32));
		result = (prime * result) + (int) (recordOffset ^ (recordOffset >>> 32));
		result = (prime * result) + (int) (recordSize ^ (recordSize >>> 32));
		result = (prime * result) + signature;
		result = (prime * result) + (int) (tableNameLength ^ (tableNameLength >>> 32));
		result = (prime * result) + (int) (totalRecordSize ^ (totalRecordSize >>> 32));
		result = (prime * result) + (int) (unk1 ^ (unk1 >>> 32));
		result = (prime * result) + (padding2 ^ (padding2 >>> 32));
		result = (prime * result) + (int) (version ^ (version >>> 32));
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
		final StructTableFileHeader other = (StructTableFileHeader) obj;
		if (fieldCount != other.fieldCount) {
			return false;
		}
		if (fieldOffset != other.fieldOffset) {
			return false;
		}
		if (lookupOffset != other.lookupOffset) {
			return false;
		}
		if (maxId != other.maxId) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (recordCount != other.recordCount) {
			return false;
		}
		if (recordOffset != other.recordOffset) {
			return false;
		}
		if (recordSize != other.recordSize) {
			return false;
		}
		if (signature != other.signature) {
			return false;
		}
		if (tableNameLength != other.tableNameLength) {
			return false;
		}
		if (totalRecordSize != other.totalRecordSize) {
			return false;
		}
		if (unk1 != other.unk1) {
			return false;
		}
		if (padding2 != other.padding2) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		return true;
	}

}
