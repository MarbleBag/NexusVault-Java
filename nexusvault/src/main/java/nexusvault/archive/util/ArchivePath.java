package nexusvault.archive.util;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectory;
import nexusvault.archive.IdxEntryNotFound;

public final class ArchivePath implements Iterable<String> {

	private final LinkedList<String> path = new LinkedList<>();

	public ArchivePath() {

	}

	private ArchivePath(List<String> path) {
		this.path.addAll(path);
	}

	public ArchivePath copy() {
		return new ArchivePath(this.path);
	}

	public int depth() {
		return path.size();
	}

	/**
	 * @return a path representing the root component of this path, or null
	 */
	public ArchivePath getRoot() {
		if (path.isEmpty()) {
			return null;
		}
		return new ArchivePath();
	}

	public ArchivePath getFirstElement() {
		if (path.isEmpty()) {
			return getRoot();
		}
		return new ArchivePath(path.subList(0, 1));
	}

	/**
	 * Returns the parent path, or null if this path does not have a parent.
	 *
	 * @return a path representing the path's parent
	 */
	public ArchivePath getParent() {
		if (path.isEmpty()) {
			return null;
		}
		final List<String> subList = path.subList(0, path.size() - 1);
		return new ArchivePath(subList);
	}

	@Override
	public Iterator<String> iterator() {
		return path.iterator();
	}

	private IdxEntry findIn(IdxDirectory dir, String entryName) {
		for (final IdxEntry entry : dir.getChilds()) {
			if (entry.getName().equalsIgnoreCase(entryName)) {
				return entry;
			}
		}
		return null;
	}

	public boolean isResolvable(IdxDirectory root) {
		IdxDirectory currentDir = root;
		final Iterator<String> it = path.iterator();

		while (it.hasNext()) {
			final String expectedEntryName = it.next();
			final IdxEntry entry = findIn(currentDir, expectedEntryName);
			if (entry == null) {
				return false;
			}

			if (entry.isFile()) {
				if (it.hasNext()) {
					return false;
				}
				return true;
			}

			currentDir = entry.asDirectory();
		}

		return true;
	}

	public IdxEntry resolve(IdxDirectory root) throws IdxEntryNotFound, IdxEntryNotADirectory {
		IdxDirectory currentDir = root;
		final Iterator<String> it = path.iterator();

		while (it.hasNext()) {
			final String step = it.next();
			final IdxEntry entry = currentDir.getEntry(step);
			if (it.hasNext()) {
				if (entry instanceof IdxDirectory) {
					currentDir = (IdxDirectory) entry;
				} else {
					throw new IdxEntryNotADirectory(String.format("'%s' of '%s' is not a directory in the archive.", step, Arrays.toString(path.toArray())));
				}
			} else {
				return entry;
			}
		}

		return root;
	}

	/**
	 *
	 * @param startIdx
	 * @param length
	 * @return
	 * @throws IllegalArgumentException
	 *             - if beginIndex is negative, or greater than or equal to the number of elements. If endIndex is less than or equal to beginIndex, or larger
	 *             than the number of elements.
	 */
	public ArchivePath subPath(int startIdx, int length) {
		if ((startIdx < 0) || (startIdx >= path.size())) {
			throw new IllegalArgumentException(""); // TODO
		}
		if ((length < 0) || ((startIdx + length) > path.size())) {
			throw new IllegalArgumentException(""); // TODO
		}

		return new ArchivePath(this.path.subList(startIdx, startIdx + length));
	}

	public String getNameOf(int elementIdx) {
		return path.get(elementIdx);
	}

	public String getLastName() {
		return path.getLast();
	}

	public ArchivePath resolve(String element) {
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

		final ArchivePath path = new ArchivePath(this.path);
		path.path.add(element);
		return path;
	}

	public void setTo(ArchivePath otherPath) {
		this.path.clear();
		this.path.addAll(otherPath.path);
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
		final ArchivePath other = (ArchivePath) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

}
