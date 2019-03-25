package nexusvault.archive;

import java.util.Arrays;
import java.util.regex.Pattern;

import nexusvault.archive.impl.BaseIdxPath;

/**
 * An immutable representation of a path, which, normally, points to a file within an archive.<br>
 * A path is independent regarding any archive, so it is possible to create a path, which may not point to a file for some, or any archives at all.
 * <p>
 * A path is a fixed sequence of named elements. Each named element will be handled in a case-insensitive manner.<br>
 * If a path does not contain any elements, thus it is empty, it is called a <code>root path</code>. Each named element represents a directory. Only the last named
 * element can represent a file or a directory. A path can also be expressed as a {@link String} of its named elements, each separated by a {@link #SEPARATOR
 * delimiting character}. The {@link String} representation of a <code>root path</code> is equivalent to an empty {@link String}.
 */
public interface IdxPath extends Cloneable, Comparable<IdxPath> {

	/**
	 * The separator <code>\</code> which is equal to the separator used in the archive specification
	 */
	public static final String SEPARATOR = "\\";

	public static IdxPath createPath() {
		return new BaseIdxPath();
	}

	public static IdxPath createPath(String firstElement) {
		return new BaseIdxPath(firstElement);
	}

	public static IdxPath createPathFrom(String elements) {
		return createPathFrom(elements, Pattern.quote(SEPARATOR));
	}

	public static IdxPath createPathFrom(String elements, String elementDelimiter) {
		final String[] split = elements.split(elementDelimiter);
		return new BaseIdxPath(Arrays.asList(split));
	}

	/**
	 * Resolves this path against the given <code>root</code>.
	 * <p>
	 * To do so, this method checks if <code>root</code> contains a child which name is equal to the first element of this path. Is this true, the process will
	 * continue with the child and the next element of this path, until all elements of this path are traversed and the last found child is returned.
	 * <p>
	 * If at any point no child can be found, a {@link IdxEntryNotFoundException} is thrown. If another named element, beside the last named element, references
	 * a {@link IdxFileLink}, thus it is impossible to reach the end of the path, a {@link IdxEntryNotADirectoryException} is thrown.
	 * <p>
	 * If this path is a root path, the returned value is the given <code>root</code>.
	 *
	 * @param root
	 *            start point of this path
	 * @return the resolved entry
	 * @throws IllegalArgumentException
	 *             If <code>root</code> is null
	 * @throws IdxEntryNotFoundException
	 *             If no entry can be found for a named element
	 * @throws IdxEntryNotADirectoryException
	 *             If the path can not reach its end, because of the occurrence of a {@link IdxFileLink}
	 *
	 * @see {@link #isResolvable(IdxEntry)}s
	 */
	IdxEntry resolve(IdxEntry root) throws IdxEntryNotFoundException;

	/**
	 * Tries to resolve this path against the given <code>root</code>.<br>
	 * Returns <code>true</code> if and only if there is a valid path, starting at <code>root</code> and ending at {@link #getLastName()} with a equally named
	 * {@link IdxEntry}, with one exception:
	 * <p>
	 * If this path is a root path (it does not contain any elements), the returned value will always be <code>true</code>, because a root path, always matches its
	 * given <code>root</code>.
	 *
	 * @param root
	 *            - entry to start
	 * @return true if the path is resolvable
	 *
	 * @throws IllegalArgumentException
	 *             If <code>root</code> is null
	 *
	 * @see #resolve(IdxEntry)
	 */
	boolean isResolvable(IdxEntry root);

	/**
	 * Resolves the given element and returns a path, which may be the same path or another path, depending on the input.
	 * <ul>
	 * <li>If the input is an empty string: The returned path is equal to this path
	 * <li>If the input is equal to the {@link #SEPARATOR separator}: The returned path is equal to {@link #getRoot()}
	 * <li>If the input is equal to <code>..</code>: The returned path is equal to {@link #getParent()}
	 * <li>If the input is a string: The returned path is equal to {@link #pathToChild(String)}
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
	IdxPath resolve(String element);

	/**
	 * Returns a new path that is an extension of this path and includes the given child as its new <code>last element</code>.
	 */
	IdxPath pathToChild(String childName);

	/**
	 * For non-root paths this method is the equivalent of
	 *
	 * <pre>
	 * path.getParent().pathToChild(newElement);
	 * </pre>
	 *
	 * @throws IdxPathInvalidException
	 *             If this path is a root path.
	 * @see #getParent()
	 * @see #pathToChild(String)
	 */
	IdxPath pathToSibling(String siblingName) throws IdxPathInvalidException;

	/**
	 * @return a <code>root path</code>
	 */
	IdxPath getRoot();

	/**
	 *
	 * @return true iff this path is a <code>root path</code>
	 */
	boolean isRoot();

	/**
	 * The parent path of this path. That is a path that is equal to this path from the named elements beginning at <code>0 to {@link #length() length} - 1</code>
	 *
	 * @return
	 */
	IdxPath getParent();

	boolean hasParent();

	/**
	 * Returns a path which is a subpath of this path, starting at the named element at <code>startIdx</code> and ending one named element before <code>endIdx</code>,
	 * thus the length of the subpath is <code>endIdx-startIdx</code>.
	 *
	 * @param startIdx
	 *            - index of the first element, starting at 0, inclusive
	 * @param endIdx
	 *            - index of the last element, exclusive
	 * @return An independent subpath of this path, containing all elements from <code>startIdx</code> to <code>endIdx-1</code>
	 * @throws IndexOutOfBoundsException
	 *             If <code>startIdx < 0 || endIdx < startIdx || {@link #length()} < endIdx</code>
	 */
	IdxPath subpath(int startIdx, int endIdx);

	/**
	 * Returns the length of this path, which is equivalent to the number of named elements. A path that contains no elements has a length of 0.
	 */
	int length();

	Iterable<String> iterateElements();

	/**
	 * Returns the {@link String} representation of this path. Returns a {@link String}, containing all named elements in sequential order, each separated by a
	 * {@link #SEPARATOR delimiting character}. If this path is a <code>root path</code>, an empty {@link String#} will be returned.
	 */
	String getFullName();

	/**
	 * Returns the last named element of this path.<br>
	 * If this path is a root path, the returned {@link String} will be empty, otherwise the returned value is equal to {@link #getNameOf(int)
	 * getNameOf(length-1)}.
	 */
	String getLastName();

	/**
	 * Returns the named element at position <code>elementIdx</code>. 0 denotes the first element. The number of elements is equal to {@link #length()}.
	 *
	 * @param elementIdx
	 *            index of the element, which name should be returned
	 * @return the name of the element at elementIdx
	 * @throws IndexOutOfBoundsException
	 *             If <code>elementIdx < 0 || {@link #length()} <= elementIdx</code>
	 */
	String getNameOf(int elementIdx);

	/**
	 * Two paths are equal, if and only if each pair of named elements fulfill {@link String#equalsIgnoreCase(String)}
	 */
	boolean equals(IdxPath path);

	/**
	 * Returns the same value as {@link #getFullName()}
	 */
	@Override
	String toString();

	/**
	 * Returns a hash code value which only relies on the sequence of the named elements this path represents
	 */
	@Override
	int hashCode();

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
	int compareTo(IdxPath otherPath);

}
