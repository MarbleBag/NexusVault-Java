/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.vault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An immutable representation of a path, which, normally, points to a file within an archive.<br>
 * A path is independent regarding any archive, so it is possible to create a path, which may not point to a file for some, or any archives at all.
 * <p>
 * A path is a fixed sequence of named elements. Each named element will be handled in a case-insensitive manner.<br>
 * If a path does not contain any elements, thus it is empty, it is called a <code>root path</code>. Each named element represents a directory. Only the last
 * named element can represent a file or a directory. A path can also be expressed as a {@link String} of its named elements, each separated by a
 * {@link #SEPARATOR delimiting character}. The {@link String} representation of a <code>root path</code> is equivalent to an empty {@link String}.
 */
public final class IdxPath implements Cloneable, Comparable<IdxPath>, Iterable<String> {

	private static final IdxPath EMPTY_PATH = new IdxPath();

	/**
	 * The separator <code>\</code> which is equal to the separator used in the archive specification
	 */
	public static final String SEPARATOR = "\\";

	public static IdxPath createPath() {
		return IdxPath.EMPTY_PATH;
	}

	public static IdxPath createPath(String... elements) {
		if (elements == null || elements.length == 0) {
			return EMPTY_PATH;
		}

		final var path = new ArrayList<String>();
		for (var i = 0; i < elements.length; ++i) {
			final var element = elements[i];
			if (element == null) {
				throw new IllegalArgumentException(String.format("%d. element must not be null", i + 1));
			}
			if (element.contains(SEPARATOR)) {
				throw new IllegalArgumentException(String.format("%d. element contains one or more separator(s) and other characters", i + 1));
			}
			if (element.isBlank()) {
				continue;
			}
			if (element.equals("..") && path.size() > 0) {
				path.remove(path.size() - 1);
				continue;
			}
			path.add(element);
		}

		return new IdxPath(path);
	}

	public static IdxPath createPath(Collection<String> elements) {
		if (elements == null || elements.size() == 0) {
			return EMPTY_PATH;
		}

		final var path = new ArrayList<String>();
		var count = -1;
		for (final var element : elements) {
			++count;

			if (element == null) {
				throw new IllegalArgumentException(String.format("%d. element must not be null", count + 1));
			}
			if (element.contains(SEPARATOR)) {
				throw new IllegalArgumentException(String.format("%d. element contains one or more separator(s) and other characters", count + 1));
			}
			if (element.isBlank()) {
				continue;
			}
			if (element.equals("..") && path.size() > 0) {
				path.remove(path.size() - 1);
				continue;
			}
			path.add(element);
		}

		return new IdxPath(path);
	}

	public static IdxPath createPathFrom(String elements) {
		return createPathFrom(elements, Pattern.quote(SEPARATOR));
	}

	public static IdxPath createPathFrom(String elements, String elementDelimiter) {
		final var split = elements.split(elementDelimiter);
		return createPath(split);

	}

	private final ArrayList<String> path;

	private IdxPath() {
		this.path = new ArrayList<>(0);
	}

	private IdxPath(List<String> elements) {
		this.path = new ArrayList<>(elements);
	}

	/**
	 * Resolves the given element and returns a path, which may be the same path or another path, depending on the input.
	 * <ul>
	 * <li>If the input is an empty string: The returned path is equal to this path
	 * <li>If the input is equal to the {@link #SEPARATOR separator}: The returned path is equal to {@link #getRoot()}
	 * <li>If the input is equal to <code>..</code>: The returned path is equal to {@link #getParent()}
	 * </ul>
	 *
	 * @param element
	 *            that should be resolved
	 * @return a new path or this path, if element is empty
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>If <code>element</code> is <code>null</code>
	 *             <li>If <code>element</code> contains at least one {@link #SEPARATOR separator}
	 *             </ul>
	 */
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

		if (element.equals("..") && hasParent()) {
			return getParent();
		}

		if (element.contains(SEPARATOR)) {
			throw new IllegalArgumentException("Element contains one or more separator(s) and other characters");
		}

		final var path = new IdxPath(this.path);
		path.path.add(element);
		return path;
	}

	/**
	 * For non-root paths this method is the equivalent of
	 *
	 * <pre>
	 * path.getParent().pathToChild(newElement);
	 * </pre>
	 *
	 * @param siblingName
	 *            name of the sibling
	 * @return the path to the sibling of this path
	 * @see #getParent()
	 */
	public IdxPath pathToSibling(String siblingName) {
		if (hasParent()) {
			return getParent().resolve(siblingName);
		} else {
			return IdxPath.createPath(siblingName);
		}
	}

	/**
	 * @return a <code>root path</code>
	 */
	public IdxPath getRoot() {
		if (isRoot()) {
			return this;
		}
		return IdxPath.createPath();
	}

	/**
	 *
	 * @return true iff this path is a <code>root path</code>
	 */
	public boolean isRoot() {
		return this.path.size() == 0;
	}

	/**
	 * The parent path of this path. In case of a {@link #isRoot() root} path, this function returns null, otherwise the returned path is equivalent to the
	 * {@link #subpath(int, int) subpath} from 0 to {@link #length()}-1.
	 *
	 * @return the parent of this path or null, iff this path is a {@link #isRoot() root} path
	 */
	public IdxPath getParent() {
		if (hasParent()) {
			return new IdxPath(this.path.subList(0, this.path.size() - 1));
		} else {
			return null;
		}
	}

	public boolean hasParent() {
		return this.path.size() > 0;
	}

	/**
	 * Returns a path which is a subpath of this path, starting at the named element at <code>startIdx</code> and ending one named element before
	 * <code>endIdx</code> (exclusive), thus the length of the subpath is <code>endIdx-startIdx</code>.
	 *
	 * @param startIdx
	 *            - index of the first element, starting at 0, inclusive
	 * @param endIdx
	 *            - index of the last element, exclusive
	 * @return An independent subpath of this path, containing all elements from <code>startIdx</code> to <code>endIdx-1</code>
	 * @throws IndexOutOfBoundsException
	 *             If <code>startIdx &lt; 0 || endIdx &lt; startIdx || {@link #length()} &lt; endIdx</code>
	 */
	public IdxPath subpath(int startIdx, int endIdx) {
		if (startIdx == 0 && endIdx == 0) {
			return getRoot();
		}

		return new IdxPath(this.path.subList(startIdx, endIdx));
	}

	/**
	 * Returns the length of this path, which is equivalent to the number of named elements. A path that contains no elements has a length of 0.
	 *
	 * @return the length
	 */
	public int length() {
		return this.path.size();
	}

	@Override
	public Iterator<String> iterator() {
		return new Iterator<>() {
			private final Iterator<String> iterator = IdxPath.this.path.iterator();

			@Override
			public boolean hasNext() {
				return this.iterator.hasNext();
			}

			@Override
			public String next() {
				return this.iterator.next();
			}
		};
	}

	/**
	 * Returns the {@link String} representation of this path. Returns a {@link String}, containing all named elements in sequential order, each separated by a
	 * {@link #SEPARATOR delimiting character}. If this path is a <code>root path</code>, an empty {@link String} will be returned.
	 *
	 * @return the full name of this path
	 */
	public String getFullName() {
		final var builder = new StringBuilder();
		final var it = this.path.iterator();
		while (it.hasNext()) {
			builder.append(it.next());
			if (it.hasNext()) {
				builder.append(SEPARATOR);
			}
		}
		return builder.toString();
	}

	/**
	 * Returns the last named element of this path.<br>
	 * If this path is a root path, the returned {@link String} will be empty, otherwise the returned value is equal to {@link #getNameOf(int)
	 * getNameOf(length-1)}.
	 *
	 * @return last named element
	 */
	public String getLastName() {
		if (isRoot()) {
			return "";
		} else {
			return this.path.get(this.path.size() - 1);
		}
	}

	/**
	 * Returns the named element at position <code>elementIdx</code>. 0 denotes the first element. The number of elements is equal to {@link #length()}.
	 *
	 * @param elementIdx
	 *            index of the element, which name should be returned
	 * @return the name of the element at elementIdx
	 * @throws IndexOutOfBoundsException
	 *             If <code>elementIdx &lt; 0 || {@link #length()} &le; elementIdx</code>
	 */
	public String getNameOf(int elementIdx) {
		return this.path.get(elementIdx);
	}

	@Override
	public IdxPath clone() {
		return new IdxPath(this.path);
	}

	/**
	 * Returns a hash code value which only relies on the sequence of the named elements this path represents
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCodePath();
		return result;
	}

	private int hashCodePath() {
		int hashCode = 1;
		for (final String p : this.path) {
			hashCode = 31 * hashCode + p.toLowerCase().hashCode();
		}
		return hashCode;
	}

	/**
	 * Two paths are equal, if and only if each pair of named elements fulfill {@link String#equalsIgnoreCase(String)}
	 *
	 * @return true if the two paths are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IdxPath)) {
			return false;
		}

		return isPathEqual(((IdxPath) obj).path.iterator());
	}

	public boolean equals(IdxPath path) {
		if (this == path) {
			return true;
		}
		if (path == null) {
			return false;
		}

		return isPathEqual(path.path.iterator());
	}

	private boolean isPathEqual(Iterator<String> otherIterator) {
		final var iterator = this.path.iterator();
		while (iterator.hasNext() && otherIterator.hasNext()) {
			final String o1 = iterator.next();
			final String o2 = otherIterator.next();
			if (!o1.equalsIgnoreCase(o2)) {
				return false;
			}
		}
		return !(iterator.hasNext() || otherIterator.hasNext());
	}

	/**
	 * Returns the same value as {@link #getFullName()}
	 */
	@Override
	public String toString() {
		return getFullName();
	}

	/**
	 * Comparison of two paths is based on the sequence of named elements.
	 * <p>
	 * To be more specific:
	 * <ul>
	 * <li>A path with less named elements compared to another path is considered to be smaller
	 * <li>If both paths have the same number of named elements, they are {@link String#compareToIgnoreCase(String) compared} pairwise
	 * </ul>
	 *
	 * @throws IllegalArgumentException
	 *             if <code>otherPath</code> is null
	 */
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

		final Iterator<String> e1 = this.path.iterator();
		final Iterator<String> e2 = otherPath.path.iterator();

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
