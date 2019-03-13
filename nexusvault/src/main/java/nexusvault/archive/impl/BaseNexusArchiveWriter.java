package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotAFileException;
import nexusvault.archive.IdxEntryNotFoundException;
import nexusvault.archive.IdxPath;
import nexusvault.archive.NexusArchiveWriter;
import nexusvault.archive.impl.IndexFile.IndexDirectoryData;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;

public class BaseNexusArchiveWriter implements NexusArchiveWriter {

	private static final LZMACodec CODEC_LZMA = new LZMACodec();
	private static final ZipCodec CODEC_ZIP = new ZipCodec();

	private IndexFile indexFile;
	private ArchiveFile archiveFile;
	private boolean isDisposed;
	private TreeDirectory root;

	@Override
	public void load(Path archiveOrIndex) throws IOException {
		final Path idxPath = ArchivePathLocator.getIndexPath(archiveOrIndex);
		final Path arcPath = ArchivePathLocator.getArchivePath(archiveOrIndex);
		load(idxPath, arcPath);
	}

	private void load(Path idxPath, Path arcPath) throws IOException {
		dispose();

		this.indexFile = IndexFile.createIndexFile();
		this.indexFile.openFile(idxPath);

		this.archiveFile = ArchiveFile.createArchiveFile();
		this.archiveFile.openFile(arcPath);

		isDisposed = false;

		initializeArchive();
	}

	private void initializeArchive() throws IOException {
		buildIndexFileTree();
	}

	private void buildIndexFileTree() throws IOException {
		final TreeDirectory root = new TreeDirectory(null, "", indexFile.getRootDirectoryIndex());
		final Queue<TreeDirectory> queue = new LinkedList<>();
		queue.add(root);

		while (!queue.isEmpty()) {
			final TreeDirectory dir = queue.poll();
			final IndexDirectoryData directoryData = indexFile.getDirectoryData(dir.getPackIdx());
			directoryData.getDirectories().stream().map(d -> new TreeDirectory(dir, d)).forEach(dir.directories::add);
			directoryData.getFileLinks().stream().map(f -> new TreeFile(dir, f)).forEach(dir.files::add);
			queue.addAll(dir.getDirectories());
		}

		this.root = root;
	}

	@Override
	public void dispose() {
		if (!isDisposed) {
			return;
		}

		try {
			flush();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (indexFile != null) {
			try {
				indexFile.closeFile();
			} catch (final IOException e) { //ignore
			} finally {
				indexFile = null;
			}
		}
		if (archiveFile != null) {
			try {
				archiveFile.closeFile();
			} catch (final IOException e) { //ignore
			} finally {
				archiveFile = null;
			}
		}

		root = null;
		// TODO

		isDisposed = true;
	}

	@Override
	public void flush() throws IOException {
		final Queue<TreeDirectory> queue = new LinkedList<>();

		if (root.hasChildWithPendingUpdate()) {
			queue.add(root);
		}

		while (!queue.isEmpty()) { // TODO implementation is not optimal/working for different update flags
			final TreeDirectory directory = queue.poll();

			if (directory.hasPendingUpdate()) {
				if (directory.hasAnyFlags(TreeEntry.FLAG_NEW)) { // this is also true for rename and create/delete of sub dirs
					if (directory.getPackIdx() == -1) {
						directory.setPackIdx(indexFile.reserveNextPackIndex());
					}

					final List<StructIdxDirectory> structDirectories = new ArrayList<>(directory.getDirectoryCount());
					for (final TreeDirectory subdir : directory.getDirectories()) {
						if (subdir.getPackIdx() == -1) {
							subdir.setPackIdx(indexFile.reserveNextPackIndex());
						}
						final StructIdxDirectory struct = new StructIdxDirectory(-1, subdir.getPackIdx());
						struct.name = subdir.getName();
						structDirectories.add(struct);
					}

					final List<StructIdxFile> structFiles = new ArrayList<>(directory.getFileCount());
					// TODO

					final IndexDirectoryData directoryData = new IndexDirectoryData(structDirectories, structFiles);
					indexFile.writeDirectoryData(directory.getPackIdx(), directoryData);
				}
			}

			if (directory.hasPendingUpdate()) {
				updateDirectory(directory);
			}

			directory.getDirectories().stream().filter(TreeDirectory::hasChildWithPendingUpdate).forEach(queue::add);

			for (final TreeDirectory child : directory.getDirectories()) {
				if (child.hasPendingUpdate()) {
					updateDirectory(directory, child);
				}
			}

			for (final TreeFile child : directory.getFiles()) {
				if (child.hasPendingUpdate()) {
					updateFile(directory, child);
				}
			}

			directory.clearFlags();
		}
	}

	private void updateDirectory(final TreeDirectory directory) {
		if (directory.hasFlags(TreeEntry.FLAG_NEW)) {
			writeNewDirectory(directory);
		} else {
			throw new UnsupportedOperationException("Not implemented yet"); // TODO
		}
	}

	private void writeNewDirectory(TreeDirectory directory) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	private void updateDirectory(TreeDirectory directory, TreeDirectory child) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	private void updateFile(TreeDirectory directory, TreeFile child) throws IOException {
		if (child.hasFlags(TreeEntry.FLAG_FILE_DATA)) {
			writeFile(directory, child);
			child.unsetFlags(TreeEntry.FLAG_FILE_DATA);
		}

		if (child.hasPendingUpdate()) {
			throw new UnsupportedOperationException("Not implemented yet"); // TODO
		}
	}

	private void writeFile(TreeDirectory directory, TreeFile file) throws IOException {
		final IntegrateableElement element = file.getElement();
		final StructIdxFile newFileAttributes = writeToArchive(element);
		newFileAttributes.name = file.getName();
		this.indexFile.setFileAttributes(directory.getPackIdx(), newFileAttributes);
	}

	private StructIdxFile writeToArchive(IntegrateableElement element) throws IOException {
		final BinaryReader originalData = element.getData();
		final IntegrateableElementConfig config = element.getConfig();
		final BinaryReader compressedData = compress(originalData, config.getCompressionType());

		final long uncompressedSize = originalData.size();
		final long compressedSize = compressedData.size();
		final byte[] hash = calculateHash(compressedData);
		if ((hash == null) || (hash.length != 20)) {
			throw new IllegalStateException(); // TODO
		}

		compressedData.seek(Seek.BEGIN, 0);

		this.archiveFile.writeArchiveData(hash, compressedData);

		return new StructIdxFile(-1, config.getCompressionType().getFlag(), System.currentTimeMillis(), uncompressedSize, compressedSize, hash, 0);
	}

	private BinaryReader compress(BinaryReader originalData, CompressionType compressionType) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	private byte[] calculateHash(BinaryReader compressedData) {
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-1");
			while (!compressedData.isEndOfData()) {
				md.update(compressedData.readInt8());
			}
			return md.digest();
		} catch (final NoSuchAlgorithmException e) {
			throw new UnsupportedOperationException("Not implemented yet"); // TODO
		}
	}

	@Override
	public boolean isDisposed() {
		return this.isDisposed;
	}

	@Override
	public void write(Collection<IntegrateableElement> elements) throws IOException {
		verifyData(elements);
		writeDataToFileTree(elements);
	}

	private void verifyData(Collection<IntegrateableElement> elements) {
		// TODO Auto-generated method stub
	}

	private void writeDataToFileTree(Collection<IntegrateableElement> elements) {
		for (final IntegrateableElement element : elements) {
			final TreeFile file = findOrCreateFile(element.getDestination());
			file.setElement(element);
		}
	}

	private TreeFile findOrCreateFile(IdxPath destination) {
		return findOrCreateFile(destination.iterateElements().iterator());
	}

	private TreeFile findOrCreateFile(Iterator<String> path) {
		TreeDirectory dir = root;
		while (path.hasNext()) {
			final String entryName = path.next();
			final TreeEntry entry = getEntryAndValidateName(dir, entryName);
			if (!path.hasNext()) {
				// this is the last element of the path.
				if (entry == null) {
					final TreeFile newFile = new TreeFile(dir, entryName);
					newFile.setFlags(TreeEntry.FLAG_NEW);
					newFile.notifyParentAboutPendingUpdate();
					dir.addFile(newFile);
					return newFile;
				} else if (entry instanceof TreeFile) {
					return (TreeFile) entry;
				} else {
					// In this case there is a directory with the same name as a file
					throw new IdxEntryNotAFileException(entryName);
				}
			} else {
				// this is not the last element of the path
				if (entry == null) {
					final TreeDirectory newDir = new TreeDirectory(dir, entryName, -1);
					newDir.setFlags(TreeEntry.FLAG_NEW);
					newDir.notifyParentAboutPendingUpdate();
					dir.addDirectory(newDir);
					dir = newDir;
				} else if (entry instanceof TreeDirectory) {
					dir = (TreeDirectory) entry;
				} else {
					// In this case there is a file with the same name as a directory
					throw new IdxEntryNotADirectoryException(entryName);
				}
			}
		}
		throw new IdxEntryNotFoundException();
	}

	private TreeEntry getEntryAndValidateName(TreeDirectory directory, String entryName) {
		final TreeEntry entry = directory.getEntry(entryName);
		if ((entry != null) && !entry.getName().equals(entryName)) {
			throw new IllegalStateException(entryName); // TODO
		}
		return entry;
	}

	private static abstract class TreeEntry {

		public static enum Flag {
			NEW,
			FILE_DATA,
			CHILD_PENDING_UPDATE
		}

		protected static final int FLAG_ = 0;

		/**
		 * Internal flag to indicate that one or multiple childs are waiting to be updated. <br>
		 * This is used as a shortcut, so it's not necessary to iterate over the whole tree.
		 */
		protected static final int FLAG_CHILD_PENDING_UPDATE = 1;

		public static final int FLAG_FILE_DATA = 2;

		/**
		 * Indicates that the directory or file is newly created
		 */
		public static final int FLAG_NEW = 4;

		public static final int FLAG_NEW_CHILD = 8;

		private final TreeDirectory parent;
		private final String name;

		private int flags;

		public TreeEntry(TreeDirectory parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		protected TreeDirectory getParent() {
			return parent;
		}

		public String getName() {
			return name;
		}

		protected void notifyParentAboutPendingUpdate() {
			final TreeDirectory parent = getParent();
			if (parent != null) {
				if (!parent.hasChildWithPendingUpdate()) {
					parent.setChildHasPendingUpdate();
					parent.notifyParentAboutPendingUpdate();
				}
			}
		}

		public final boolean hasPendingUpdate() {
			return hasAnyFlagsBeside(FLAG_CHILD_PENDING_UPDATE);
		}

		protected final void setFlags(int flags) {
			this.flags |= flags;
		}

		protected final void unsetFlags(int flags) {
			this.flags &= ~flags;
		}

		protected final boolean hasFlags(int flags) {
			return (this.flags & flags) == flags;
		}

		protected final boolean hasAnyFlags(int flags) {
			return (this.flags & flags) != 0;
		}

		protected final boolean hasAnyFlagsBeside(int flags) {
			return hasAnyFlags(~flags);
		}

		protected final void clearFlags() {
			flags = 0;
		}

		protected final boolean hasAnyFlag() {
			return flags != 0;
		}

	}

	private static final class TreeDirectory extends TreeEntry {

		private final int packIdx;
		private final List<TreeDirectory> directories;
		private final List<TreeFile> files;

		public TreeDirectory(TreeDirectory parent, String name, int packIdx) {
			super(parent, name);
			this.packIdx = packIdx;
			this.directories = new ArrayList<>();
			this.files = new ArrayList<>();
		}

		public void setPackIdx(long reserveNextPackIndex) {
			// TODO Auto-generated method stub

		}

		public TreeDirectory(TreeDirectory parent, StructIdxDirectory directory) {
			this(parent, directory.name, directory.directoryHeaderIdx);
		}

		public int getPackIdx() {
			return packIdx;
		}

		protected boolean hasChildWithPendingUpdate() {
			return hasFlags(FLAG_CHILD_PENDING_UPDATE);
		}

		public void setChildHasPendingUpdate() {
			setFlags(FLAG_CHILD_PENDING_UPDATE);
		}

		protected void addDirectory(TreeDirectory subdir) {
			directories.add(subdir);
			setFlags(FLAG_NEW_CHILD);
			notifyParentAboutPendingUpdate();
		}

		protected void addFile(TreeFile file) {
			files.add(file);
			setFlags(FLAG_NEW_CHILD);
			notifyParentAboutPendingUpdate();
		}

		public int getDirectoryCount() {
			return directories.size();
		}

		public int getFileCount() {
			return files.size();
		}

		public List<TreeDirectory> getDirectories() {
			return Collections.unmodifiableList(directories);
		}

		public List<TreeFile> getFiles() {
			return Collections.unmodifiableList(files);
		}

		public TreeDirectory getDirectory(String entryName) {
			for (final TreeDirectory entry : directories) {
				if (entry.getName().equalsIgnoreCase(entryName)) {
					return entry;
				}
			}
			return null;
		}

		public TreeFile getFile(String entryName) {
			for (final TreeFile entry : files) {
				if (entry.getName().equalsIgnoreCase(entryName)) {
					return entry;
				}
			}
			return null;
		}

		public TreeEntry getEntry(String entryName) {
			final TreeEntry entry = getDirectory(entryName);
			return entry != null ? entry : getFile(entryName);
		}

		public int countAllDirectories() {
			return 1 + directories.stream().mapToInt(TreeDirectory::countAllDirectories).sum();
		}

		public int countAllFiles() {
			return files.size() + directories.stream().mapToInt(TreeDirectory::countAllFiles).sum();
		}

	}

	private static final class TreeFile extends TreeEntry {
		private final StructIdxFile file;
		private IntegrateableElement element;

		public TreeFile(TreeDirectory parent, StructIdxFile file) {
			super(parent, file.name);
			this.file = file;
		}

		public TreeFile(TreeDirectory parent, String name) {
			super(parent, name);
			this.file = null;
		}

		public void setElement(IntegrateableElement element) {
			this.element = element;
			setFlags(FLAG_FILE_DATA);
			notifyParentAboutPendingUpdate();
		}

		public IntegrateableElement getElement() {
			return element;
		}
	}

}
