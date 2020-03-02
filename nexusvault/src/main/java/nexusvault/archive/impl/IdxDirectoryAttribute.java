package nexusvault.archive.impl;

import java.util.Objects;

public final class IdxDirectoryAttribute {
	private String name;
	private final int directoryIndex;

	public IdxDirectoryAttribute(String name, int directoryIndex) {
		super();
		this.name = name;
		this.directoryIndex = directoryIndex;
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
		final IdxDirectoryAttribute other = (IdxDirectoryAttribute) obj;
		return (directoryIndex == other.directoryIndex) && Objects.equals(name, other.name);
	}

	public int getDirectoryIndex() {
		return directoryIndex;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(directoryIndex, name);
	}

	/**
	 * @param name
	 *            the new name
	 * @throws IllegalArgumentException
	 *             if <code>name</code> is null
	 */
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("'name' must not be null");
		}
		this.name = name;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("IdxDirectoryAttribute [name=");
		builder.append(name);
		builder.append(", directoryIndex=");
		builder.append(directoryIndex);
		builder.append("]");
		return builder.toString();
	}

}
