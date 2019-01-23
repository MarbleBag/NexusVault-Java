package nexusvault.pack.util;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nexusvault.pack.index.IdxDirectory;
import nexusvault.pack.index.IdxEntry;
import nexusvault.pack.index.IdxEntryNotADirectory;
import nexusvault.pack.index.IdxEntryNotFound;

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
	 * Returns the root component of this path as a Path object, or null if this path does not have a root component.
	 *
	 * @return a path representing the root component of this path, or null
	 */
	public ArchivePath getRoot() {
		if (path.isEmpty()) {
			return null;
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
		// TODO
		return null;
	}

	public String getLastName() {
		// TODO
		return null;
	}

	public ArchivePath resolve(String element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		if (element.isEmpty()) {
			return this;
		}

		if (element.equals(File.separator)) {
			return toRoot();
		}

		if (element.equals("..")) {
			return toParent();
		}

		path.add(element);

		return this;
	}

	public void setTo(ArchivePath otherPath) {
		this.path.clear();
		this.path.addAll(otherPath.path);
	}

	public ArchivePath toParent() {
		if (!path.isEmpty()) {
			path.removeLast();
		}
		return this;
	}

	public ArchivePath toRoot() {
		path.clear();
		return this;
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

}
