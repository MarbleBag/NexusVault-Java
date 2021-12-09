package nexusvault.archive.struct;

import java.util.Arrays;
import java.util.Objects;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructIdxFile {

	static {
		if (StructUtil.sizeOf(StructIdxFile.class) != 0x38) {
			throw new StructException();
		}
	}

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIdxFile.class);

	/**
	 * A {@code nameOffset} of <code>-1</code> indicates, that no offset is set
	 */
	@Order(1)
	@StructField(DataType.UBIT_32)
	public long nameOffset; // 0x000

	@Order(2)
	@StructField(DataType.BIT_32)
	public int flags; // 0x004

	@Order(3)
	@StructField(DataType.UBIT_64)
	public long writeTime; // 0x008

	@Order(4)
	@StructField(DataType.UBIT_64)
	public long uncompressedSize; // 0x010

	@Order(5)
	@StructField(DataType.UBIT_64)
	public long compressedSize; // 0x018

	@Order(6)
	@StructField(value = DataType.BIT_8, length = 20)
	public byte[] hash; // 0x020

	/**
	 * Unknown field. Sometimes used, sometimes not.
	 */
	@Order(7)
	@StructField(DataType.BIT_32)
	public int unk_034; // 0x034

	public StructIdxFile() {

	}

	public StructIdxFile(long nameOffset, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk_034) {
		this.nameOffset = nameOffset;
		this.flags = flags;
		this.writeTime = writeTime;
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.hash = hash;
		this.unk_034 = unk_034;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.hash);
		result = prime * result + Objects.hash(this.compressedSize, this.flags, this.nameOffset, this.uncompressedSize, this.unk_034, this.writeTime);
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
		final StructIdxFile other = (StructIdxFile) obj;
		return this.compressedSize == other.compressedSize && this.flags == other.flags && Arrays.equals(this.hash, other.hash)
				&& this.nameOffset == other.nameOffset && this.uncompressedSize == other.uncompressedSize && this.unk_034 == other.unk_034
				&& this.writeTime == other.writeTime;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructIdxFile [nameOffset=");
		builder.append(this.nameOffset);
		builder.append(", flags=");
		builder.append(this.flags);
		builder.append(", writeTime=");
		builder.append(this.writeTime);
		builder.append(", uncompressedSize=");
		builder.append(this.uncompressedSize);
		builder.append(", compressedSize=");
		builder.append(this.compressedSize);
		builder.append(", hash=");
		builder.append(Arrays.toString(this.hash));
		builder.append(", unk_034=");
		builder.append(this.unk_034);
		builder.append("]");
		return builder.toString();
	}

}