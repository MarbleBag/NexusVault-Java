package nexusvault.format.tbl.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.BIT_64;
import static kreed.reflection.struct.DataType.UBIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.NotUsedForPaddingException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.StructException;

public final class StructTableFileHeader {

	static {
		if (StructUtil.sizeOf(StructTableFileHeader.class) != 0x60) {
			throw new StructException();
		}
	}

	public static final int SIGNATURE = 'D' << 24 | 'T' << 16 | 'B' << 8 | 'L';

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructTableFileHeader.class) /* 96 */;

	/** int32; 'DTBL' */
	@Order(1)
	@StructField(BIT_32)
	public int signature; // 0x000

	/** uint32; */
	@Order(2)
	@StructField(BIT_32)
	public long version; // 0x004

	/** uint32; number of UTF-16 encoded characters, aligned to 16 byte and null terminated */
	@Order(3)
	@StructField(UBIT_32) // maybe a int64
	public long nameLength; // 0x008

	/** uint32; */
	@Order(4)
	@StructField(BIT_32)
	private int padding1; // 0x00C

	/** uint64; */
	@Order(5)
	@StructField(UBIT_64)
	public long unk1; // 0x010

	/** uint32; Size of one record in bytes */
	@Order(6)
	@StructField(UBIT_32) // maybe a int64
	public long recordSize; // 0x018

	/** uint32; */
	@Order(7)
	@StructField(BIT_32)
	private int padding2; // 0x01C

	/** uint64; number of fields */
	@Order(8)
	@StructField(UBIT_64)
	public long fieldCount; // 0x020

	/** uint64; Start offset for the first field */
	@Order(9)
	@StructField(UBIT_64)
	public long fieldOffset; // 0x028

	/** uint32; number of records */
	@Order(10)
	@StructField(UBIT_32) // maybe a int64
	public long recordCount; // 0x030

	/** uint32; */
	@Order(11)
	@StructField(BIT_32)
	private int padding3; // 0x034

	/** uint64; Size of all records in bytes */
	@Order(12)
	@StructField(UBIT_64)
	public long totalRecordSize; // 0x038

	/** uint64; Start offset for the first record */
	@Order(13)
	@StructField(UBIT_64)
	public long recordOffset; // 0x040

	/** uint64; */
	@Order(14)
	@StructField(UBIT_64)
	public long lookupCount; // 0x048

	/**
	 * uint64;
	 *
	 * record Id to record idx, -1 means no index
	 **/
	@Order(15)
	@StructField(UBIT_64)
	public long lookupOffset; // 0x050

	/** uint64; */
	@Order(16)
	@StructField(BIT_64)
	private long padding4; // 0x058

	public StructTableFileHeader() {
	}

	public StructTableFileHeader(BinaryReader reader) {
		final long headerStart = reader.getPosition();
		final long headerEnd = headerStart + SIZE_IN_BYTES;

		this.signature = reader.readInt32(); // o:4
		if (this.signature != SIGNATURE) {
			throw new SignatureMismatchException("Table Header", SIGNATURE, this.signature);
		}

		this.version = reader.readUInt32(); // o:8
		this.nameLength = reader.readUInt32(); // o:12
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
		this.lookupCount = reader.readInt64(); // o:80
		this.lookupOffset = reader.readInt64(); // o:88
		this.padding4 = reader.readInt64(); // o:96

		if (this.padding1 != 0) {
			throw new NotUsedForPaddingException("padding1");
		}
		if (this.padding2 != 0) {
			throw new NotUsedForPaddingException("padding2");
		}
		if (this.padding3 != 0) {
			throw new NotUsedForPaddingException("padding3");
		}
		if (this.padding4 != 0) {
			throw new NotUsedForPaddingException("padding4");
		}

		if (reader.getPosition() != headerEnd) {
			throw new StructException("Expected number of bytes " + SIZE_IN_BYTES + " read bytes: " + (reader.getPosition() - headerStart));
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TableFileHeader [signature=");
		builder.append(this.signature);
		builder.append(", version=");
		builder.append(this.version);
		builder.append(", tableNameLength=");
		builder.append(this.nameLength);
		builder.append(", unk1=");
		builder.append(this.unk1);
		builder.append(", recordSize=");
		builder.append(this.recordSize);
		builder.append(", fieldCount=");
		builder.append(this.fieldCount);
		builder.append(", fieldOffset=");
		builder.append(this.fieldOffset);
		builder.append(", recordCount=");
		builder.append(this.recordCount);
		builder.append(", totalRecordSize=");
		builder.append(this.totalRecordSize);
		builder.append(", recordOffset=");
		builder.append(this.recordOffset);
		builder.append(", maxId=");
		builder.append(this.lookupCount);
		builder.append(", lookupOffset=");
		builder.append(this.lookupOffset);
		builder.append(", unk2=");
		builder.append(this.padding2);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.fieldCount ^ this.fieldCount >>> 32);
		result = prime * result + (int) (this.fieldOffset ^ this.fieldOffset >>> 32);
		result = prime * result + (int) (this.lookupOffset ^ this.lookupOffset >>> 32);
		result = prime * result + (int) (this.lookupCount ^ this.lookupCount >>> 32);
		result = prime * result + (int) (this.recordCount ^ this.recordCount >>> 32);
		result = prime * result + (int) (this.recordOffset ^ this.recordOffset >>> 32);
		result = prime * result + (int) (this.recordSize ^ this.recordSize >>> 32);
		result = prime * result + this.signature;
		result = prime * result + (int) (this.nameLength ^ this.nameLength >>> 32);
		result = prime * result + (int) (this.totalRecordSize ^ this.totalRecordSize >>> 32);
		result = prime * result + (int) (this.unk1 ^ this.unk1 >>> 32);
		result = prime * result + (this.padding2 ^ this.padding2 >>> 32);
		result = prime * result + (int) (this.version ^ this.version >>> 32);
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
		if (this.fieldCount != other.fieldCount) {
			return false;
		}
		if (this.fieldOffset != other.fieldOffset) {
			return false;
		}
		if (this.lookupOffset != other.lookupOffset) {
			return false;
		}
		if (this.lookupCount != other.lookupCount) {
			return false;
		}
		if (this.recordCount != other.recordCount) {
			return false;
		}
		if (this.recordOffset != other.recordOffset) {
			return false;
		}
		if (this.recordSize != other.recordSize) {
			return false;
		}
		if (this.signature != other.signature) {
			return false;
		}
		if (this.nameLength != other.nameLength) {
			return false;
		}
		if (this.totalRecordSize != other.totalRecordSize) {
			return false;
		}
		if (this.unk1 != other.unk1) {
			return false;
		}
		if (this.padding2 != other.padding2) {
			return false;
		}
		if (this.version != other.version) {
			return false;
		}
		return true;
	}

}
