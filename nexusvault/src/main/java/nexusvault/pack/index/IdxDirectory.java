package nexusvault.pack.index;

import java.io.File;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public final class IdxDirectory extends IdxEntry {

	protected final long packHeaderIdx;
	protected List<IdxEntry> childs;

	public IdxDirectory(IdxDirectory parent, String name, long subDirectoryHeaderIdx) {
		super(parent, name);
		this.packHeaderIdx = subDirectoryHeaderIdx;
	}

	public List<IdxEntry> getChilds() {
		if (this.childs == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(childs);
	}

	public int getChildCount() {
		return this.childs != null ? this.childs.size() : 0;
	}

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

	public List<IdxDirectory> getSubDirectories() {
		if (childs == null) {
			return Collections.emptyList();
		}
		return getChilds().parallelStream().filter(f -> f instanceof IdxDirectory).map(f -> (IdxDirectory) f).collect(Collectors.toList());
	}

	public List<IdxFileLink> getFiles() {
		if (childs == null) {
			return Collections.emptyList();
		}
		return getChilds().parallelStream().filter(f -> f instanceof IdxFileLink).map(f -> (IdxFileLink) f).collect(Collectors.toList());
	}

	public IdxEntry getEntry(String path) throws IdxEntryNotFound {
		if (childs == null) {
			throw new IdxEntryNotFound(path);
		}

		final int sepIdx = path.indexOf(File.separator);
		if (sepIdx < 0) {
			for (final IdxEntry child : getChilds()) {
				if (child.getName().equalsIgnoreCase(path)) {
					return child;
				}
			}
			throw new IdxEntryNotFound(path);
		}

		final String dir = path.substring(0, sepIdx);
		path = path.substring(sepIdx + 1);
		for (final IdxEntry child : getChilds()) {
			if (child instanceof IdxDirectory) {
				if (dir.equalsIgnoreCase(child.getName())) {
					return ((IdxDirectory) child).getEntry(path);
				}
			}
		}
		throw new IdxEntryNotFound(path);
	}

	public IdxFileLink getFile(String path) {
		final IdxEntry entry = getEntry(path);
		if (!(entry instanceof IdxFileLink)) {
			throw new IdxEntryNotAFile(path);
		}
		return (IdxFileLink) entry;
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
		result = (prime * result) + ((childs == null) ? 0 : childs.hashCode());
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
		final IdxDirectory other = (IdxDirectory) obj;
		if (childs == null) {
			if (other.childs != null) {
				return false;
			}
		} else if (!childs.equals(other.childs)) {
			return false;
		}
		if (packHeaderIdx != other.packHeaderIdx) {
			return false;
		}
		return true;
	}

}