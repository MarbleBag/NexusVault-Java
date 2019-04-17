package nexusvault.archive.struct;

import java.util.Objects;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class StructIdxDirectory {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIdxDirectory.class);

	@Order(1)
	@StructField(DataType.UBIT_32)
	public int nameOffset; // 0x000

	@Order(2)
	@StructField(DataType.UBIT_32)
	public int directoryIndex; // 0x004

	public StructIdxDirectory() {

	}

	public StructIdxDirectory(int nameOffset, int directoryHeaderIdx) {
		this.nameOffset = nameOffset;
		directoryIndex = directoryHeaderIdx;
	}

	@Override
	public int hashCode() {
		return Objects.hash(directoryIndex, nameOffset);
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
		return (directoryIndex == other.directoryIndex) && (nameOffset == other.nameOffset);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructIdxDirectory [nameOffset=");
		builder.append(nameOffset);
		builder.append(", directoryIndex=");
		builder.append(directoryIndex);
		builder.append("]");
		return builder.toString();
	}

}
