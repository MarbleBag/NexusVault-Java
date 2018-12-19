package nexusvault.pack.index;

import java.io.File;

public abstract class IdxEntry {
	protected final IdxDirectory parent;
	protected final String name;

	protected IdxEntry(IdxDirectory parent, String name) {
		if (name == null) {
			throw new IllegalArgumentException("'name' must not be null.");
		}
		if (name.contains(File.separator)) {
			throw new IllegalArgumentException("Name can not contain the file seperator '" + File.separator + "'.");
		}

		this.parent = parent;
		this.name = name;
	}

	public final String getName() {
		return name;
	}

	public final IdxDirectory getParent() {
		return parent;
	}

	public String fullName() {
		if (this.parent != null) {
			final String parentName = this.parent.fullName();
			if (parentName.isEmpty()) {
				return name;
			} else {
				return parentName + File.separator + name;
			}
		} else {
			return name;
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("IdxEntry [name=");
		builder.append(fullName());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + ((parent == null) ? 0 : parent.hashCode());
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
		final IdxEntry other = (IdxEntry) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!parent.equals(other.parent)) {
			return false;
		}
		return true;
	}

	public final boolean isFile() {
		return this instanceof IdxFileLink;
	}

	public final boolean isDir() {
		return this instanceof IdxDirectory;
	}

	public final IdxFileLink asFile() {
		return (IdxFileLink) this;
	}

	public final IdxDirectory asDirectory() {
		return (IdxDirectory) this;
	}
}