package nexusvault.archive.impl;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotAFileException;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.IdxPath;

abstract class BaseIdxEntry implements IdxEntry {

	private final BaseIdxDirectory parent;
	private final String name;

	/**
	 * @throws IllegalArgumentException
	 *             if <code>name</code> is null or contains an {@link IdxPath#SEPARATOR illegal character}
	 */
	protected BaseIdxEntry(BaseIdxDirectory parent, String name) {
		if (name == null) {
			throw new IllegalArgumentException("'name' must not be null.");
		}

		if (name.contains(IdxPath.SEPARATOR)) {
			throw new IllegalArgumentException("Name can not contain the file seperator '" + IdxPath.SEPARATOR + "'.");
		}

		this.parent = parent;
		this.name = name;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final BaseIdxDirectory getParent() {
		return parent;
	}

	@Override
	public String getFullName() {
		final String parentName = getParent().getFullName();
		if (parentName.isEmpty()) {
			return getName();
		} else {
			return parentName + IdxPath.SEPARATOR + getName();
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
	public final BaseIdxFileLink asFile() throws IdxEntryNotAFileException {
		if (isFile()) {
			return (BaseIdxFileLink) this;
		} else {
			throw new IdxEntryNotAFileException(getFullName());
		}
	}

	@Override
	public final BaseIdxDirectory asDirectory() throws IdxEntryNotADirectoryException {
		if (isDir()) {
			return (BaseIdxDirectory) this;
		} else {
			throw new IdxEntryNotADirectoryException(getFullName());
		}
	}

	@Override
	public IdxPath getPath() {
		return getParent().getPath().resolve(getName());
	}

	@Override
	public BaseNexusArchiveReader getArchive() {
		return getParent().getArchive();
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
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
