package nexusvault.format.tbl;

import java.util.Arrays;
import java.util.Objects;

public final class Table {
	public String name;
	public Column[] columns;
	public Object[][] entries;
	public int[] lookup;
	public long unk1;

	public Table(String name, Column[] columns, Object[][] entries, int[] lookup, long unk1) {
		this.name = name;
		this.columns = columns;
		this.entries = entries;
		this.lookup = lookup;
		this.unk1 = unk1;
	}

	public Object[] getById(int id) {
		final var idx = this.lookup[id];
		if (idx == -1) {
			return null;
		}
		return this.entries[idx];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.columns);
		result = prime * result + Arrays.deepHashCode(this.entries);
		result = prime * result + Arrays.hashCode(this.lookup);
		result = prime * result + Objects.hash(this.name, this.unk1);
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
		final Table other = (Table) obj;
		return Arrays.equals(this.columns, other.columns) && Arrays.deepEquals(this.entries, other.entries) && Arrays.equals(this.lookup, other.lookup)
				&& Objects.equals(this.name, other.name) && this.unk1 == other.unk1;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Table [name=");
		builder.append(this.name);
		builder.append(", columns=");
		builder.append(Arrays.toString(this.columns));
		builder.append(", entries=");
		builder.append(Arrays.toString(this.entries));
		builder.append(", lookup=");
		builder.append(Arrays.toString(this.lookup));
		builder.append(", unk1=");
		builder.append(this.unk1);
		builder.append("]");
		return builder.toString();
	}

}
