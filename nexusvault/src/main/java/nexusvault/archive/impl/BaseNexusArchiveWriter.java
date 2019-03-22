package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import kreed.io.util.ByteBufferBinaryReader;
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
	private boolean isLoaded;
	private TreeDirectory root;

	@Override
	public void load(Path archiveOrIndex) throws IOException {
		final Path idxPath = ArchivePathLocator.getIndexPath(archiveOrIndex);
		final Path arcPath = ArchivePathLocator.getArchivePath(archiveOrIndex);
		load(idxPath, arcPath);
	}

	private void load(Path idxPath, Path arcPath) throws IOException {
		dispose();

		indexFile = IndexFile.createIndexFile();
		indexFile.openFile(idxPath);

		archiveFile = ArchiveFile.createArchiveFile();
		archiveFile.openFile(arcPath);

		isLoaded = true;

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
			final IndexDirectoryData directoryData = indexFile.getDirectoryData(dir.getDirectoryIndex());
			directoryData.getDirectories().stream().map(d -> new TreeDirectory(dir, d)).forEach(dir.directories::add);
			directoryData.getFileLinks().stream().map(f -> new TreeFile(dir, f)).forEach(dir.files::add);
			queue.addAll(dir.getDirectories());
		}

		this.root = root;
	}

	@Override
	public void dispose() {
		if (!isLoaded) {
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
			} catch (final IOException e) { // ignore
			} finally {
				indexFile = null;
			}
		}
		if (archiveFile != null) {
			try {
				archiveFile.closeFile();
			} catch (final IOException e) { // ignore
			} finally {
				archiveFile = null;
			}
		}

		root = null;
		// TODO

		isLoaded = false;
	}

	@Override
	public void flush() throws IOException {
		processDirectory(root);
	}

	// TODO (re)writes EVERYTHING in its current state
	private void processDirectory(TreeDirectory directory) throws IOException {
		for (final TreeDirectory subdir : directory.getDirectories()) {
			if (subdir.isWaitingForChildWriteUpdate() || subdir.isWaitingForDirectoryUpdate()) {
				processDirectory(subdir);
			}

			if (subdir.getDirectoryIndex() == -1) {
				throw new IllegalStateException();
			}
		}

		if (isDirectoryNew(directory) || isDirectoryChildNew(directory) || hasMoreThanNFilesChanged(directory, 1)) {

			indexFile.enableWriteMode();
			archiveFile.enableWriteMode();

			final List<StructIdxDirectory> directories = new ArrayList<>();
			final List<StructIdxFile> files = new ArrayList<>();

			for (final TreeDirectory subdir : directory.getDirectories()) {
				final StructIdxDirectory sdir = new StructIdxDirectory(-1, subdir.getDirectoryIndex());
				sdir.name = subdir.getName();
				directories.add(sdir);
			}

			for (final TreeFile file : directory.getFiles()) {
				StructIdxFile sfile = file.getFileInfo();

				if (file.hasFlags(TreeEntry.FLAG_NEW)) {
					sfile = writeToArchive(null, file.getFileData());
					sfile.name = file.getName();
				} else if (file.hasFlags(TreeEntry.FLAG_NEW_DATA)) {
					if (sfile == null) {
						throw new IllegalStateException(); // TODO
					}
					sfile = writeToArchive(sfile.hash, file.getFileData());
					sfile.name = file.getName();
				} else {
					if (sfile == null) {
						throw new IllegalStateException(); // TODO
					}
				}

				file.setFileInfo(sfile);
				files.add(sfile);
				file.clearFlags();
			}

			final IndexDirectoryData directoryData = new IndexDirectoryData(directories, files);

			if (directory.getDirectoryIndex() == -1) {
				final long directoryIndex = indexFile.writeDirectoryData(directoryData);
				directory.setDirectoryIndex(directoryIndex);
			} else {
				indexFile.writeDirectoryData(directory.getDirectoryIndex(), directoryData);
			}

		} else {
			int index = 0;
			for (final TreeFile file : directory.getFiles()) {
				if (file.hasFlags(TreeEntry.FLAG_NEW_DATA)) {
					StructIdxFile sfile = file.getFileInfo();
					sfile = writeToArchive(sfile.hash, file.getFileData());
					indexFile.overwriteFileAttribute(directory.getDirectoryIndex(), index, sfile.hash, sfile);
				}
				index++;
			}
		}
	}

	private boolean hasMoreThanNFilesChanged(TreeDirectory directory, int i) {
		for (final TreeFile file : directory.getFiles()) {
			if (file.hasFlags(TreeEntry.FLAG_NEW_DATA)) {
				i -= 1;
			}
		}
		return i < 0;
	}

	private boolean isDirectoryChildNew(TreeDirectory directory) {
		return directory.hasFlags(TreeEntry.FLAG_NEW); // TODO
	}

	private boolean isDirectoryNew(TreeDirectory directory) {
		return directory.hasFlags(TreeEntry.FLAG_NEW);
	}

	private StructIdxFile writeToArchive(byte[] oldHash, IntegrateableElement element) throws IOException {
		final BinaryReader uncompressedData = element.getData();
		final IntegrateableElementConfig config = element.getConfig();
		final BinaryReader compressedData = compress(uncompressedData, config.getCompressionType());

		final long uncompressedSize = uncompressedData.size();
		final long compressedSize = compressedData.size();
		final byte[] hash = computeHash(compressedData);

		if (oldHash == null) {
			archiveFile.writeArchiveData(hash, compressedData);
		} else {
			archiveFile.replaceArchiveData(oldHash, hash, compressedData);
		}

		return new StructIdxFile(-1, config.getCompressionType().getFlag(), System.currentTimeMillis(), uncompressedSize, compressedSize, hash, 0);
	}

	private BinaryReader compress(BinaryReader originalData, CompressionType compressionType) {
		originalData.seek(Seek.BEGIN, 0);
		switch (compressionType) {
			case NONE:
				return originalData;
			case LZMA: {
				final ByteBuffer compressed = CODEC_LZMA.encode(originalData);
				return new ByteBufferBinaryReader(compressed);
			}
			case ZIP: {
				final ByteBuffer compressed = CODEC_ZIP.encode(originalData);
				return new ByteBufferBinaryReader(compressed);
			}
			default:
				throw new UnsupportedOperationException("Not implemented yet"); // TODO
		}
	}

	private byte[] computeHash(BinaryReader compressedData) {
		compressedData.seek(Seek.BEGIN, 0);
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-1");
			while (!compressedData.isEndOfData()) {
				md.update(compressedData.readInt8());
			}

			final byte[] hash = md.digest();
			compressedData.seek(Seek.BEGIN, 0);
			if ((hash == null) || (hash.length != 20)) {
				throw new IllegalStateException(); // TODO
			}
			return hash;
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean isDisposed() {
		return isLoaded;
	}

	@Override
	public void write(Collection<? extends IntegrateableElement> elements) throws IOException {
		verifyData(elements);
		completeFileTree(elements);
	}

	public void replace(IdxPath src, IdxPath destination, boolean deleteOldDestination) {

	}

	private void verifyData(Collection<? extends IntegrateableElement> elements) {
		// TODO Auto-generated method stub
	}

	private void completeFileTree(Collection<? extends IntegrateableElement> elements) {
		for (final IntegrateableElement element : elements) {
			final TreeFile file = findOrCreateFile(element.getDestination());
			file.setFileData(element);
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
					newFile.notifyParentAboutPendingWriteUpdate();
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
					newDir.notifyParentAboutPendingWriteUpdate();
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
		if ((entry != null) && entry.hasFlags(TreeEntry.FLAG_NEW) && !entry.getName().equals(entryName)) {
			throw new IllegalStateException(entryName); // TODO
		}
		return entry;
	}

	private static abstract class TreeEntry {

		public static enum PendingUpdateFlag {
			NEW,
			RENAMED,
			REMOVED,
			ADDED,
			CHILD_CHANGED,
			CHILD_WAITS_FOR_UPDATE,
		}

		public static final int FLAG_NEW = 1;
		public static final int FLAG_ADDED = 2;
		public static final int FLAG_NEW_DATA = 4;
		public static final int FLAG_CHILD_CHANGED = 8;
		public static final int FLAG_CHILD_WAITS_FOR_UPDATE = 16;

		private final TreeDirectory parent;
		private final String name;

		private int flags = 0;

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

		protected final void notifyParentAboutPendingWriteUpdate() {
			final TreeDirectory parent = getParent();
			if (parent != null) {
				if (!parent.isWaitingForChildWriteUpdate()) {
					parent.setWaitingForChildWriteUpdate();
					parent.notifyParentAboutPendingWriteUpdate();
				}
			}
		}

		protected final boolean isWaitingForDirectoryUpdate() {
			return hasAnyFlagsBeside(FLAG_CHILD_WAITS_FOR_UPDATE);
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

		protected final boolean hasAnyFlags() {
			return flags != 0;
		}

	}

	private static final class TreeDirectory extends TreeEntry {

		private int directoryIndex;
		private final List<TreeDirectory> directories;
		private final List<TreeFile> files;

		public TreeDirectory(TreeDirectory parent, String name, int packIdx) {
			super(parent, name);
			directoryIndex = packIdx;
			directories = new ArrayList<>();
			files = new ArrayList<>();
		}

		protected void setDirectoryIndex(long packIdx) {
			directoryIndex = (int) packIdx;
		}

		public TreeDirectory(TreeDirectory parent, StructIdxDirectory directory) {
			this(parent, directory.name, directory.directoryIndex);
		}

		/**
		 * @return the directory index, or <tt>-1</tt> if no directory index is set
		 */
		public int getDirectoryIndex() {
			return directoryIndex;
		}

		protected boolean isWaitingForChildWriteUpdate() {
			return hasFlags(FLAG_CHILD_WAITS_FOR_UPDATE);
		}

		public void setWaitingForChildWriteUpdate() {
			setFlags(FLAG_CHILD_WAITS_FOR_UPDATE);
		}

		protected void addDirectory(TreeDirectory subdir) {
			directories.add(subdir);
			subdir.setFlags(FLAG_ADDED);
			subdir.notifyParentAboutPendingWriteUpdate();
		}

		protected void addFile(TreeFile file) {
			files.add(file);
			file.setFlags(FLAG_ADDED);
			file.notifyParentAboutPendingWriteUpdate();
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
		private StructIdxFile file;
		private IntegrateableElement element;

		public TreeFile(TreeDirectory parent, StructIdxFile file) {
			super(parent, file.name);
			this.file = file;
		}

		public TreeFile(TreeDirectory parent, String name) {
			super(parent, name);
			file = null;
		}

		protected void setFileInfo(StructIdxFile info) {
			file = info;
		}

		public StructIdxFile getFileInfo() {
			return file;
		}

		public void setFileData(IntegrateableElement element) {
			this.element = element;
			setFlags(FLAG_NEW_DATA);
			notifyParentAboutPendingWriteUpdate();
			getParent().setFlags(FLAG_CHILD_CHANGED);
		}

		public IntegrateableElement getFileData() {
			return element;
		}
	}

}
