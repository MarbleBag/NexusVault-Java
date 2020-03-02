package nexusvault.archive.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotFoundException;
import nexusvault.archive.IdxPath;
import nexusvault.archive.IdxPathInvalidException;

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
		if (root == null) {
			throw new IllegalArgumentException("'root' must not be null");
		}

		if (isRoot()) {
			return root;
		}

		if (root.isFile()) {
			throw new IdxEntryNotADirectoryException("For a non-empty path the root element needs to be a directory");
		}

		// IdxDirectory cDir = root.asDirectory();
		// final Iterator<String> it = path.iterator();
		//
		// while (it.hasNext()) {
		// final String nextEntryName = it.next();
		// final IdxEntry nextEntry = cDir.getEntry(nextEntryName);
		// if (it.hasNext()) { // path goes deeper
		// if (nextEntry.isFile()) {
		// throw new IdxEntryNotADirectoryException(
		// String.format("'%s' of '%s' is not a directory in the archive.", nextEntryName, Arrays.toString(path.toArray())));
		// } else {
		// cDir = nextEntry.asDirectory();
		// }
		// } else {
		// return nextEntry;
		// }
		// }
		//
		// return root;

		final var result = tryToResolve(root);
		return result.orElseThrow(() -> {
			final var reachablePath = findResolveablePath(root);
			final var unreachablePath = subpath(reachablePath.length(), length());
			return new IdxEntryNotFoundException(String.format("Resolved path to '%s', unable to reach element '%s'", reachablePath, unreachablePath));
		});
	}

	@Override
	public Optional<IdxEntry> tryToResolve(IdxEntry root) {
		if (root == null) {
			throw new IllegalArgumentException("'root' must not be null");
		}

		if (isRoot()) { // shortcut - always matches
			return Optional.of(root);
		}

		if (root.isFile()) { // shortcut - never matches
			return Optional.empty();
		}

		IdxEntry entry = root.asDirectory();
		final Iterator<String> it = path.iterator();
		while (it.hasNext()) {
			if (entry.isFile()) { // reached a file, before we reached end of path - mismatch
				return Optional.empty();
			}
			final var namedElement = it.next();
			final var directory = entry.asDirectory();
			if (!directory.hasEntry(namedElement)) { // entry not found - mismatch
				return Optional.empty();
			}
			entry = directory.getEntry(namedElement);
		}
		return Optional.of(entry);
	}

	@Override
	public IdxPath findResolveablePath(IdxEntry root) {
		if (root == null) {
			throw new IllegalArgumentException("'root' must not be null");
		}

		if (isRoot()) { // shortcut
			return this;
		}

		if (root.isFile()) { // shortcut
			return getRoot();
		}

		IdxEntry entry = root.asDirectory();
		final Iterator<String> it = path.iterator();
		while (it.hasNext()) {
			if (entry.isFile()) { // reached a file, before we reached end of path - mismatch, create path
				return entry.getPath();
			}
			final var namedElement = it.next();
			final var directory = entry.asDirectory();
			if (!directory.hasEntry(namedElement)) { // entry not found - mismatch
				return directory.getPath();
			}
			entry = directory.getEntry(namedElement);
		}
		return this; // full match
	}

	@Override
	public boolean isResolvable(IdxEntry root) {
		if (root == null) {
			throw new IllegalArgumentException("'root' must not be null");
		}

		if (isRoot()) { // path which only contains the root is always resolvable
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
	public IdxPath pathToSibling(String siblingName) throws IdxPathInvalidException {
		if (hasParent()) {
			return getParent().pathToChild(siblingName);
		} else {
			throw new IdxPathInvalidException("Path has no parent");
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
		return new Iterable<>() {
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
			return "";
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
		result = (prime * result) + hashCodePath();
		return result;
	}

	private int hashCodePath() {
		int hashCode = 1;
		for (final String p : path) {
			hashCode = (31 * hashCode) + p.toLowerCase().hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IdxPath)) {
			return isPathEqual(((IdxPath) obj).iterateElements().iterator());
		}
		return false;
	}

	@Override
	public boolean equals(IdxPath path) {
		if (this == path) {
			return true;
		}
		if (path == null) {
			return false;
		}
		return isPathEqual(path.iterateElements().iterator());
	}

	private boolean isPathEqual(BaseIdxPath other) {
		if (path == other.path) {
			return true;
		}
		return isPathEqual(other.path.listIterator());
	}

	private boolean isPathEqual(Iterator<String> e2) {
		final ListIterator<String> e1 = path.listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			final String o1 = e1.next();
			final String o2 = e2.next();
			if (!o1.equalsIgnoreCase(o2)) {
				return false;
			}
		}
		return !(e1.hasNext() || e2.hasNext());
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

	@Override
	public int compareTo(IdxPath otherPath) {
		if (otherPath == null) {
			throw new IllegalArgumentException("'otherPath' must not be null");
		}

		if (length() < otherPath.length()) {
			return -1;
		}
		if (length() > otherPath.length()) {
			return 1;
		}

		final Iterator<String> e1 = iterateElements().iterator();
		final Iterator<String> e2 = otherPath.iterateElements().iterator();

		while (e1.hasNext() && e2.hasNext()) {
			final String o1 = e1.next();
			final String o2 = e2.next();
			final int order = o1.compareToIgnoreCase(o2);
			if (order != 0) {
				return order;
			}
		}

		return 0;
	}

}
