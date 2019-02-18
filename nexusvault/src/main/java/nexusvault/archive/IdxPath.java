package nexusvault.archive;

import java.util.Arrays;

import nexusvault.archive.impl.BaseIdxPath;

/**
 * An immutable representation of a path within an archive file. A path is independent regarding its use with an archive, hence it can be used with different
 * archives as well as saved and loaded for later use. <br>
 * Its elements are stored separately and only certain functions make use of a {@link #SEPARATOR separator} to separate elements for a more human readable
 * representation of a path.
 */
public interface IdxPath extends Cloneable {

	/**
	 * The separator <tt>\</tt> which is equal to the separator the implementer of the archive file used.
	 */
	public static final String SEPARATOR = "\\";

	public static IdxPath createPath() {
		return new BaseIdxPath();
	}

	public static IdxPath createPath(String firstElement) {
		return new BaseIdxPath(firstElement);
	}

	public static IdxPath createPathFrom(String elements) {
		return createPathFrom(elements, SEPARATOR);
	}

	public static IdxPath createPathFrom(String elements, String elementDelimiter) {
		final String[] split = elements.split(elementDelimiter);
		return new BaseIdxPath(Arrays.asList(split));
	}

	IdxEntry resolve(IdxEntry root) throws IdxEntryNotFoundException;

	boolean isResolvable(IdxEntry root);

	/**
	 * Resolves the given element and returns a path, which may be the same path or another path, depending on the input.
	 * <ul>
	 * <li>If the input is an empty string: The returned path is equal to this path
	 * <li>If the input is equal to the {@link #SEPARATOR separator}: The returned path is equal to {@link #getRoot()}
	 * <li>If the input is equal to <tt>..</tt>: The returned path is equal to {@link #getParent()}
	 * <li>If the input is a string: The returned path is equal to {@link #pathToChild(String)}
	 * </ul>
	 *
	 * @param element
	 *            that should be resolved
	 * @return a new path or this path, if element is empty
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>If <tt>element</tt> is <tt>null</tt>
	 *             <li>If <tt>element</tt> contains multiple separator or a separator combined with other characters
	 *             </ul>
	 */
	IdxPath resolve(String element);

	/**
	 * Returns a new path that is an extension of this path and includes the given child as its new <tt>last element</tt>.
	 */
	IdxPath pathToChild(String childName);

	/**
	 * For non-root paths this method is the equivalent of
	 *
	 * <pre>
	 * path.getParent().pathToChild(newElement);
	 * </pre>
	 *
	 * @throws InvalidIdxPathException
	 *             If this path is a root path.
	 * @see #getParent()
	 * @see #pathToChild(String)
	 */
	IdxPath pathToSibling(String siblingName) throws InvalidIdxPathException;

	IdxPath getRoot();

	boolean isRoot();

	/**
	 * The parent path of this path. That is a path that is equal to this path from the elements beginning at <tt>0 to {@link #length() length} - 1</tt>
	 *
	 * @return
	 */
	IdxPath getParent();

	boolean hasParent();

	/**
	 * Returns a path which is a subpath of this path, starting at the element at <tt>startIdx</tt> and ending one element before <tt>endIdx</tt>, thus the
	 * length of the subpath is <tt>endIdx-startIdx</tt>.
	 *
	 * @param startIdx
	 *            - index of the first element, starting at 0, inclusive
	 * @param endIdx
	 *            - index of the last element, exclusive
	 * @return An independent subpath of this path, containing all elements from <tt>startIdx</tt> to <tt>endIdx-1</tt>
	 * @throws IndexOutOfBoundsException
	 *             If <tt>startIdx < 0 || endIdx < startIdx || {@link #length()} < endIdx</tt>
	 */
	IdxPath subpath(int startIdx, int endIdx);

	/**
	 * Length of the path, which is equivalent to the number of elements of a path. <br>
	 * A path that contains no elements, thus a root path, has a length of 0.
	 */
	int length();

	Iterable<String> iterateElements();

	/**
	 * Returns the full path in a single string. Each element is separated by the system independent {@link SEPARATOR separator} In case this is a root path,
	 * this function will return an empty string.
	 */
	String getFullName();

	/**
	 * Returns the name of the last element of this path. <br>
	 * The returned value of this method is equivalent to {@link #getNameOf(int) getNameOf(length-1)}.
	 *
	 * @return
	 */
	String getLastName();

	/**
	 * Returns the name of the element at position <tt>elementIdx</tt>. 0 denotes the first element. The number of elements is equal to {@link #length()}, thus
	 * a root path has no elements.
	 *
	 * @param elementIdx
	 *            index of the element, which name should be returned
	 * @return the name of the element at elementIdx
	 */
	String getNameOf(int elementIdx);

	@Override
	boolean equals(Object path);

	@Override
	String toString();

	@Override
	int hashCode();

}
