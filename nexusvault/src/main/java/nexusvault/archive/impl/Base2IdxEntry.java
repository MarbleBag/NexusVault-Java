package nexusvault.archive.impl;

import java.io.File;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.NexusArchive;

@Deprecated
abstract class Base2IdxEntry implements IdxEntry {
	protected final Base2IdxDirectory parent;
	protected final String name;

	protected Base2IdxEntry(Base2IdxDirectory parent, String name) {
		if (name == null) {
			throw new IllegalArgumentException("'name' must not be null.");
		}
		if (name.contains(File.separator)) {
			throw new IllegalArgumentException("Name can not contain the file seperator '" + File.separator + "'.");
		}

		this.parent = parent;
		this.name = name;
	}

	@Override
	public NexusArchive getArchive() {
		return null;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final Base2IdxDirectory getParent() {
		return parent;
	}

	@Override
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
		final Base2IdxEntry other = (Base2IdxEntry) obj;
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

	@Override
	public final boolean isFile() {
		return this instanceof IdxFileLink;
	}

	@Override
	public final boolean isDir() {
		return this instanceof IdxDirectory;
	}

	@Override
	public final Base2IdxFileLink asFile() {
		return (Base2IdxFileLink) this;
	}

	@Override
	public final Base2IdxDirectory asDirectory() {
		return (Base2IdxDirectory) this;
	}
}