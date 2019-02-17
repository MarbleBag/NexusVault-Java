package nexusvault.archive;

import java.io.File;
import java.util.Arrays;

import nexusvault.archive.impl.BaseIdxPath;

public interface IdxPath extends Cloneable {

	public static IdxPath createPath() {
		return new BaseIdxPath();
	}

	public static IdxPath createPath(String firstElement) {
		return new BaseIdxPath(firstElement);
	}

	public static IdxPath createPathFrom(String elements) {
		return createPathFrom(elements, File.separator);
	}

	public static IdxPath createPathFrom(String elements, String elementDelimiter) {
		final String[] split = elements.split(elementDelimiter);
		return new BaseIdxPath(Arrays.asList(split));
	}

	IdxEntry resolve(IdxEntry root) throws IdxEntryNotFound;

	boolean isResolvable(IdxEntry root);

	IdxPath resolve(String element);

	IdxPath pathToChild(String childName);

	IdxPath pathToSibling(String siblingName);

	IdxPath getRoot();

	boolean isRoot();

	IdxPath getParent();

	boolean hasParent();

	IdxPath subpath(int start, int end);

	int depth();

	Iterable<String> iterateElements();

	String getFullName();

	String getLastName();

	String getNameOf(int elementIdx);

	IdxPath clone();

	@Override
	boolean equals(Object path);

	@Override
	String toString();

	@Override
	int hashCode();

}
