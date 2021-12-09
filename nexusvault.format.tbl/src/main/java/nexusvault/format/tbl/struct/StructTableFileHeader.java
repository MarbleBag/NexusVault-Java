package nexusvault.format.tbl.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.BIT_64;
import static kreed.reflection.struct.DataType.UBIT_64;

import java.util.Objects;

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

	/** 'DTBL' */
	@Order(1)
	@StructField(BIT_32)
	public int signature; // 0x000

	@Order(2)
	@StructField(BIT_32)
	public long version; // 0x004

	/** number of UTF-16 encoded characters, aligned to 16 byte and null terminated */
	@Order(3)
	@StructField(UBIT_64) // maybe a int64
	public long nameLength; // 0x008

	/**  */
	@Order(4)
	@StructField(UBIT_64)
	public long unk1; // 0x010

	/** Size of a single record in bytes */
	@Order(5)
	@StructField(UBIT_64) // maybe a int64
	public long recordSize; // 0x018

	/** Number of fields */
	@Order(6)
	@StructField(UBIT_64)
	public long fieldCount; // 0x020

	/** Start offset for the first field */
	@Order(7)
	@StructField(UBIT_64)
	public long fieldOffset; // 0x028

	/** Number of records */
	@Order(8)
	@StructField(UBIT_64) // maybe a int64
	public long recordCount; // 0x030

	/** Size of all records in bytes */
	@Order(9)
	@StructField(UBIT_64)
	public long totalRecordsSize; // 0x038

	/** Start offset for the first record */
	@Order(10)
	@StructField(UBIT_64)
	public long recordOffset; // 0x040

	/** id to index lookup */
	@Order(11)
	@StructField(UBIT_64)
	public long lookupCount; // 0x048

	@Order(12)
	@StructField(UBIT_64)
	public long lookupOffset; // 0x050

	@Order(13)
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
		this.nameLength = reader.readInt64(); // o:12
		this.unk1 = reader.readInt64(); // o:24
		this.recordSize = reader.readInt64(); // o:28
		this.fieldCount = reader.readInt64(); // o:40
		this.fieldOffset = reader.readInt64(); // o:48
		this.recordCount = reader.readInt64(); // o:52
		this.totalRecordsSize = reader.readInt64(); // o:64
		this.recordOffset = reader.readInt64(); // o:72
		this.lookupCount = reader.readInt64(); // o:80
		this.lookupOffset = reader.readInt64(); // o:88
		this.padding4 = reader.readInt64(); // o:96

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
		builder.append("StructTableFileHeader [signature=");
		builder.append(this.signature);
		builder.append(", version=");
		builder.append(this.version);
		builder.append(", nameLength=");
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
		builder.append(this.totalRecordsSize);
		builder.append(", recordOffset=");
		builder.append(this.recordOffset);
		builder.append(", lookupCount=");
		builder.append(this.lookupCount);
		builder.append(", lookupOffset=");
		builder.append(this.lookupOffset);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.fieldCount, this.fieldOffset, this.lookupCount, this.lookupOffset, this.nameLength, this.recordCount, this.recordOffset,
				this.recordSize, this.signature, this.totalRecordsSize, this.unk1, this.version);
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
		return this.fieldCount == other.fieldCount && this.fieldOffset == other.fieldOffset && this.lookupCount == other.lookupCount
				&& this.lookupOffset == other.lookupOffset && this.nameLength == other.nameLength && this.recordCount == other.recordCount
				&& this.recordOffset == other.recordOffset && this.recordSize == other.recordSize && this.signature == other.signature
				&& this.totalRecordsSize == other.totalRecordsSize && this.unk1 == other.unk1 && this.version == other.version;
	}

}
