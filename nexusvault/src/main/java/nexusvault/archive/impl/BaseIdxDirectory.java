package nexusvault.archive.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxEntryNotADirectory;
import nexusvault.archive.IdxEntryNotAFile;
import nexusvault.archive.IdxEntryNotFound;
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
	public BaseIdxEntry getEntry(String entryName) throws IdxEntryNotFound {
		final Optional<BaseIdxEntry> entry = getChildsInternal().stream().filter(f -> f.getName().equals(entryName)).findFirst();
		if (!entry.isPresent()) {
			throw new IdxEntryNotFound(entryName);
		}
		return entry.get();
	}

	@Override
	public BaseIdxDirectory getDirectory(String directoryName) throws IdxEntryNotFound, IdxEntryNotADirectory {
		return getEntry(directoryName).asDirectory();
	}

	@Override
	public BaseIdxFileLink getFileLink(String fileLinkName) throws IdxEntryNotFound, IdxEntryNotAFile {
		return getEntry(fileLinkName).asFile();
	}

	@Override
	public BaseIdxDirectory createDirectory(String directoryName) {
		final BaseIdxDirectory subDirectory = getArchive().createDirectory(this, directoryName);
		getChildsInternal().add(subDirectory);
		return subDirectory;
	}

	@Override
	public BaseIdxFileLink createFileLink(String fileLinkName, ByteBuffer data, int flags) {
		final BaseIdxFileLink fileLink = getArchive().createFileLink(this, fileLinkName, data, flags);
		getChildsInternal().add(fileLink);
		return fileLink;
	}

	private void initializeChilds() {
		final List<BaseIdxEntry> childs = getArchive().loadDirectory(this);
		this.childs = new ArrayList<>(childs);
	}

	@Override
	public void removeEntry(String entryName) throws IdxEntryNotFound {
		final BaseIdxEntry entry = getEntry(entryName);
		// TODO Auto-generated method stub

	}

}
