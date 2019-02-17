package nexusvault.archive.impl;

import java.io.File;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectory;
import nexusvault.archive.IdxEntryNotAFile;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.IdxPath;

abstract class BaseIdxEntry implements IdxEntry {

	private final BaseIdxDirectory parent;
	private final String name;

	protected BaseIdxEntry(BaseIdxDirectory parent, String name) {
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
	public final String getName() {
		return name;
	}

	@Override
	public final IdxDirectory getParent() {
		return parent;
	}

	@Override
	public String getFullName() {
		final String parentName = getParent().getFullName();
		if (parentName.isEmpty()) {
			return getName();
		} else {
			return parentName + File.separator + name;
		}
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
	public final BaseIdxFileLink asFile() throws IdxEntryNotAFile {
		if (isFile()) {
			return (BaseIdxFileLink) this;
		} else {
			throw new IdxEntryNotAFile(getFullName());
		}
	}

	@Override
	public final BaseIdxDirectory asDirectory() throws IdxEntryNotADirectory {
		if (isDir()) {
			return (BaseIdxDirectory) this;
		} else {
			throw new IdxEntryNotADirectory(getFullName());
		}
	}

	@Override
	public IdxPath getPath() {
		return parent.getPath().resolve(getName());
	}

	@Override
	public BaseNexusArchive getArchive() {
		return parent.getArchive();
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
		return this == obj;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("IdxEntry [name=");
		builder.append(getFullName());
		builder.append("]");
		return builder.toString();
	}

}
