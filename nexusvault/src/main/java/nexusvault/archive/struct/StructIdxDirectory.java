package nexusvault.archive.struct;

import java.util.Objects;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructIdxDirectory {

	static {
		if (StructUtil.sizeOf(StructIdxDirectory.class) != 0x08) {
			throw new StructException();
		}
	}

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIdxDirectory.class);

	@Order(1)
	@StructField(DataType.UBIT_32)
	public int nameOffset; // 0x000

	@Order(2)
	@StructField(DataType.UBIT_32)
	public int directoryIndex; // 0x004

	public StructIdxDirectory() {

	}

	public StructIdxDirectory(int nameOffset, int directoryIndex) {
		this.nameOffset = nameOffset;
		this.directoryIndex = directoryIndex;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.directoryIndex, this.nameOffset);
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
		final StructIdxDirectory other = (StructIdxDirectory) obj;
		return this.directoryIndex == other.directoryIndex && this.nameOffset == other.nameOffset;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructIdxDirectory [nameOffset=");
		builder.append(this.nameOffset);
		builder.append(", directoryIndex=");
		builder.append(this.directoryIndex);
		builder.append("]");
		return builder.toString();
	}

}
