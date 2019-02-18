package nexusvault.archive.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotFoundException;
import nexusvault.archive.IdxPath;
import nexusvault.archive.InvalidIdxPathException;

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
	public IdxEntry resolve(IdxEntry root) throws IdxEntryNotFoundException {
		if (isRoot()) {
			return root;
		}

		if (root.isFile()) {
			throw new IdxEntryNotADirectoryException("For a non-empty path the root element needs to be a directory");
		}

		IdxDirectory cDir = root.asDirectory();
		final Iterator<String> it = path.iterator();

		while (it.hasNext()) {
			final String nextEntryName = it.next();
			final IdxEntry nextEntry = cDir.getEntry(nextEntryName);
			if (it.hasNext()) { // path goes deeper
				if (nextEntry.isFile()) {
					throw new IdxEntryNotADirectoryException(
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
	public IdxPath pathToSibling(String siblingName) throws InvalidIdxPathException {
		if (hasParent()) {
			return getParent().pathToChild(siblingName);
		} else {
			throw new InvalidIdxPathException("Path has no parent");
		}
	}

	@Override
	public IdxPath getRoot() {
		if (isRoot()) {
			return this;
		}
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
	public IdxPath subpath(int startIdx, int endIdx) {
		if ((startIdx == 0) && (endIdx == 0)) {
			return getRoot();
		}

		return new BaseIdxPath(path.subList(startIdx, endIdx));
	}

	@Override
	public int length() {
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
		final StringBuilder builder = new StringBuilder();
		// builder.append(SEPARATOR);
		final Iterator<String> it = path.iterator();
		while (it.hasNext()) {
			final String step = it.next();
			builder.append(step);
			if (it.hasNext()) {
				builder.append(SEPARATOR);
			}
		}
		return builder.toString();
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
		return getFullName();
	}

	@Override
	public IdxPath resolve(String element) {
		if (element == null) {
			throw new IllegalArgumentException("'element' must not be null");
		}

		if (element.isEmpty()) {
			return this;
		}

		if (element.equals(SEPARATOR)) {
			return getRoot();
		}

		if (element.equals("..")) {
			return getParent();
		}

		if (element.contains(SEPARATOR)) {
			throw new IllegalArgumentException("Element contains one or more separator(s) and other characters");
		}

		final BaseIdxPath path = new BaseIdxPath(this.path);
		path.path.add(element);
		return path;
	}

}
