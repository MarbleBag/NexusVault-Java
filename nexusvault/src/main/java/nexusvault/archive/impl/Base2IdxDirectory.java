package nexusvault.archive.impl;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotAFileException;
import nexusvault.archive.IdxEntryNotFoundException;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.IdxPath;
import nexusvault.archive.NexusArchive;

@Deprecated
class Base2IdxDirectory extends Base2IdxEntry implements IdxDirectory {

	protected final long packHeaderIdx;
	protected List<Base2IdxEntry> childs;

	protected Base2IdxDirectory(Base2IdxDirectory parent, String name, long subDirectoryHeaderIdx) {
		super(parent, name);
		this.packHeaderIdx = subDirectoryHeaderIdx;
	}

	@Override
	public List<IdxEntry> getChilds() {
		if (this.childs == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(childs);
	}

	public void setChilds(List<Base2IdxEntry> childs) {
		this.childs = childs;
	}

	@Override
	public int getChildCount() {
		return this.childs != null ? this.childs.size() : 0;
	}

	@Override
	public List<IdxEntry> getChildsDeep() {
		final List<IdxEntry> results = new LinkedList<>();
		final Deque<IdxDirectory> fringe = new LinkedList<>();
		fringe.add(this);

		while (!fringe.isEmpty()) {
			final IdxDirectory dir = fringe.pollFirst();
			results.add(dir);
			for (final IdxEntry child : dir.getChilds()) {
				if (child instanceof IdxDirectory) {
					fringe.addFirst((IdxDirectory) child);
				} else {
					results.add(child);
				}
			}
		}

		return results;
	}

	@Override
	public int countSubTree() {
		final Deque<IdxDirectory> fringe = new LinkedList<>();
		fringe.add(this);
		int result = 1;
		while (!fringe.isEmpty()) {
			final IdxDirectory dir = fringe.pollFirst();
			for (final IdxEntry child : dir.getChilds()) {
				result += 1;
				if (child instanceof IdxDirectory) {
					fringe.addFirst((IdxDirectory) child);
				}
			}
		}
		return result;
	}

	@Override
	public String fullName() {
		if (this.parent != null) {
			final String parentName = this.parent.fullName();
			if (parentName.isEmpty()) {
				return name;
			} else {
				return parentName + File.separator + name;
			}
		} else {
			return name;
		}
	}

	@Override
	public List<IdxDirectory> getSubDirectories() {
		if (childs == null) {
			return Collections.emptyList();
		}
		return getChilds().parallelStream().filter(f -> f instanceof IdxDirectory).map(f -> (IdxDirectory) f).collect(Collectors.toList());
	}

	@Override
	public List<IdxFileLink> getFiles() {
		if (childs == null) {
			return Collections.emptyList();
		}
		return getChilds().parallelStream().filter(f -> f instanceof IdxFileLink).map(f -> (IdxFileLink) f).collect(Collectors.toList());
	}

	@Override
	public Base2IdxEntry getEntry(String path) throws IdxEntryNotFoundException {
		if (childs == null) {
			throw new IdxEntryNotFoundException(path);
		}

		final int sepIdx = path.indexOf(File.separator);
		if (sepIdx < 0) {
			for (final IdxEntry child : getChilds()) {
				if (child.getName().equalsIgnoreCase(path)) {
					return (Base2IdxEntry) child;
				}
			}
			throw new IdxEntryNotFoundException(path);
		}

		final String dir = path.substring(0, sepIdx);
		path = path.substring(sepIdx + 1);
		for (final IdxEntry child : getChilds()) {
			if (child instanceof IdxDirectory) {
				if (dir.equalsIgnoreCase(child.getName())) {
					return ((Base2IdxDirectory) child).getEntry(path);
				}
			}
		}
		throw new IdxEntryNotFoundException(path);
	}

	public Base2IdxFileLink getFile(String path) throws IdxEntryNotFoundException, IdxEntryNotAFileException {
		final IdxEntry entry = getEntry(path);
		if (!(entry instanceof IdxFileLink)) {
			throw new IdxEntryNotAFileException(path);
		}
		return (Base2IdxFileLink) entry;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("IdxDirectory [");
		builder.append("packHeaderIdx=").append(packHeaderIdx);
		builder.append(", name=").append(fullName());
		builder.append(", #childs=").append(getChilds().size());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + (int) (packHeaderIdx ^ (packHeaderIdx >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Base2IdxDirectory other = (Base2IdxDirectory) obj;
		if (packHeaderIdx != other.packHeaderIdx) {
			return false;
		}
		return true;
	}

	@Override
	public IdxPath getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NexusArchive getArchive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasEntry(String entryName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IdxDirectory createDirectory(String directoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdxFileLink setData(String fileName, ByteBuffer data, int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IdxDirectory> getDirectories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdxDirectory getDirectory(String directoryName) throws IdxEntryNotFoundException, IdxEntryNotADirectoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdxFileLink getFileLink(String fileLinkName) throws IdxEntryNotFoundException, IdxEntryNotAFileException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdxFileLink createFileLink(String fileLinkName, ByteBuffer data, int flags) {
		// TODO Auto-generated method stub
		return null;
	}

}