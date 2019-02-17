package nexusvault.archive.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectory;
import nexusvault.archive.IdxEntryNotFound;
import nexusvault.archive.IdxPath;

public final class BaseIdxPath implements IdxPath {

	private final ArrayList<String> path;

	public BaseIdxPath() {
		path = new ArrayList<>(0);
	}

	public BaseIdxPath(List<String> elements) {
		path = new ArrayList<>(elements);
	}

	public BaseIdxPath(String element, String... elements) {
		final int size = 1 + (elements != null ? elements.length : 0);
		path = new ArrayList<>(size);
		path.add(element);
		if (elements != null) {
			for (final String e : elements) {
				path.add(e);
			}
		}
	}

	@Override
	public IdxEntry resolve(IdxEntry root) throws IdxEntryNotFound {
		if (isRoot()) {
			return root;
		}

		if (root.isFile()) {
			throw new IdxEntryNotADirectory("For a non-empty path the root element needs to be a directory");
		}

		IdxDirectory cDir = root.asDirectory();
		final Iterator<String> it = path.iterator();

		while (it.hasNext()) {
			final String nextEntryName = it.next();
			final IdxEntry nextEntry = cDir.getEntry(nextEntryName);
			if (it.hasNext()) { // path goes deeper
				if (nextEntry.isFile()) {
					throw new IdxEntryNotADirectory(
							String.format("'%s' of '%s' is not a directory in the archive.", nextEntryName, Arrays.toString(path.toArray())));
				} else {
					cDir = nextEntry.asDirectory();
				}
			} else {
				return nextEntry;
			}
		}

		return root;
	}

	@Override
	public boolean isResolvable(IdxEntry root) {
		if (isRoot()) {
			return true;
		}

		if (root.isFile()) {
			return false;
		}

		IdxDirectory currentDir = root.asDirectory();
		final Iterator<String> it = path.iterator();

		while (it.hasNext()) {
			final String expectedEntryName = it.next();
			if (!currentDir.hasEntry(expectedEntryName)) {
				return false;
			}

			final IdxEntry entry = currentDir.getEntry(expectedEntryName);
			if (entry.isFile()) {
				return !it.hasNext();
			}

			currentDir = entry.asDirectory();
		}

		return true;
	}

	@Override
	public IdxPath pathToChild(String childName) {
		return resolve(childName);
	}

	@Override
	public IdxPath pathToSibling(String siblingName) {
		if (hasParent()) {
			return getParent().pathToChild(siblingName);
		} else {
			return new BaseIdxPath(siblingName);
		}
	}

	@Override
	public IdxPath getRoot() {
		return new BaseIdxPath();
	}

	@Override
	public boolean isRoot() {
		return path.size() == 0;
	}

	@Override
	public IdxPath getParent() {
		if (hasParent()) {
			return new BaseIdxPath(path.subList(0, path.size() - 1));
		} else {
			return null;
		}
	}

	@Override
	public boolean hasParent() {
		return path.size() > 0;
	}

	@Override
	public IdxPath subpath(int start, int end) {
		return new BaseIdxPath(path.subList(start, end));
	}

	@Override
	public int depth() {
		return path.size();
	}

	@Override
	public Iterable<String> iterateElements() {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return path.iterator();
			}
		};
	}

	@Override
	public String getFullName() {
		return String.join(File.separator, path);
	}

	@Override
	public String getLastName() {
		if (isRoot()) {
			return null;
		} else {
			return path.get(path.size() - 1);
		}
	}

	@Override
	public String getNameOf(int elementIdx) {
		return path.get(elementIdx);
	}

	@Override
	public IdxPath clone() {
		return new BaseIdxPath(path);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((path == null) ? 0 : path.hashCode());
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
		final BaseIdxPath other = (BaseIdxPath) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(File.separator);

		final Iterator<String> it = path.iterator();
		while (it.hasNext()) {
			final String step = it.next();
			builder.append(step);
			if (it.hasNext()) {
				builder.append(File.separator);
			}
		}

		return builder.toString();
	}

	@Override
	public IdxPath resolve(String element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		if (element.isEmpty()) {
			return this;
		}

		if (element.equals(File.separator)) {
			return getRoot();
		}

		if (element.equals("..")) {
			return getParent();
		}

		final BaseIdxPath path = new BaseIdxPath(this.path);
		path.path.add(element);
		return path;
	}

}
