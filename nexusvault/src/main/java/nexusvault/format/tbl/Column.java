package nexusvault.format.tbl;

import java.util.Objects;

public final class Column {

	public String name;
	public ColumnType type;
	public long unk2;

	public Column(String name, ColumnType type, long unk2) {
		this.name = name;
		this.type = type;
		this.unk2 = unk2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.name, this.unk2);
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
		final Column other = (Column) obj;
		return this.type == other.type && Objects.equals(this.name, other.name) && this.unk2 == other.unk2;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Column [name=");
		builder.append(this.name);
		builder.append(", dataType=");
		builder.append(this.type);
		builder.append(", unk2=");
		builder.append(this.unk2);
		builder.append("]");
		return builder.toString();
	}

}
