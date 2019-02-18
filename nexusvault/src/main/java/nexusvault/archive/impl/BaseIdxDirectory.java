package nexusvault.archive.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotAFileException;
import nexusvault.archive.IdxEntryNotFoundException;
import nexusvault.archive.IdxFileLink;

class BaseIdxDirectory extends BaseIdxEntry implements IdxDirectory {

	private final long headerIndex;
	protected List<BaseIdxEntry> childs;

	protected BaseIdxDirectory(BaseIdxDirectory parent, String directoryName, long headerIndex) {
		super(parent, directoryName);
		this.headerIndex = headerIndex;
	}

	private List<BaseIdxEntry> getChildsInternal() {
		if (childs == null) {
			initializeChilds();
		}
		return childs;
	}

	protected long getDirectoryPackIndex() {
		return headerIndex;
	}

	@Override
	public List<IdxEntry> getChilds() {
		return Collections.unmodifiableList(getChildsInternal());
	}

	@Override
	public int getChildCount() {
		return getChildsInternal().size();
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
					fringe.addLast((IdxDirectory) child);
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
			final List<IdxEntry> childs = dir.getChilds();
			result += childs.size();
			for (final IdxEntry child : childs) {
				if (child instanceof IdxDirectory) {
					fringe.addLast((IdxDirectory) child);
				}
			}
		}
		return result;
	}

	@Override
	public List<IdxDirectory> getDirectories() {
		return getChildsInternal().stream().filter(f -> f instanceof IdxDirectory).map(f -> (IdxDirectory) f).collect(Collectors.toList());
	}

	@Override
	public List<IdxFileLink> getFiles() {
		return getChildsInternal().stream().filter(f -> f instanceof IdxFileLink).map(f -> (IdxFileLink) f).collect(Collectors.toList());
	}

	@Override
	public boolean hasEntry(String entryName) {
		return getChildsInternal().stream().anyMatch(f -> f.getName().equals(entryName));
	}

	@Override
	public BaseIdxEntry getEntry(String entryName) throws IdxEntryNotFoundException {
		final Optional<BaseIdxEntry> entry = getChildsInternal().stream().filter(f -> f.getName().equals(entryName)).findFirst();
		if (!entry.isPresent()) {
			throw new IdxEntryNotFoundException(entryName);
		}
		return entry.get();
	}

	@Override
	public BaseIdxDirectory getDirectory(String directoryName) throws IdxEntryNotFoundException, IdxEntryNotADirectoryException {
		return getEntry(directoryName).asDirectory();
	}

	@Override
	public BaseIdxFileLink getFileLink(String fileLinkName) throws IdxEntryNotFoundException, IdxEntryNotAFileException {
		return getEntry(fileLinkName).asFile();
	}

	private void initializeChilds() {
		final List<BaseIdxEntry> childs = getArchive().loadDirectory(this);
		this.childs = new ArrayList<>(childs);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("IdxDirectory [");
		builder.append("headerIdx=").append(getDirectoryPackIndex());
		builder.append(", name=").append(getFullName());
		builder.append(", #childs=").append(getChilds().size());
		builder.append("]");
		return builder.toString();
	}

}
