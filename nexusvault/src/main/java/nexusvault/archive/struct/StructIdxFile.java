package nexusvault.archive.struct;

import java.util.Arrays;
import java.util.Objects;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class StructIdxFile {

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
		result = (prime * result) + Arrays.hashCode(hash);
		result = (prime * result) + Objects.hash(compressedSize, flags, nameOffset, uncompressedSize, unk_034, writeTime);
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
		return (compressedSize == other.compressedSize) && (flags == other.flags) && Arrays.equals(hash, other.hash) && (nameOffset == other.nameOffset)
				&& (uncompressedSize == other.uncompressedSize) && (unk_034 == other.unk_034) && (writeTime == other.writeTime);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructIdxFile [nameOffset=");
		builder.append(nameOffset);
		builder.append(", flags=");
		builder.append(flags);
		builder.append(", writeTime=");
		builder.append(writeTime);
		builder.append(", uncompressedSize=");
		builder.append(uncompressedSize);
		builder.append(", compressedSize=");
		builder.append(compressedSize);
		builder.append(", hash=");
		builder.append(Arrays.toString(hash));
		builder.append(", unk_034=");
		builder.append(unk_034);
		builder.append("]");
		return builder.toString();
	}

}