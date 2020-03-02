package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import nexusvault.archive.CompressionType;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotAFileException;
import nexusvault.archive.IdxEntryNotFoundException;
import nexusvault.archive.IdxPath;
import nexusvault.archive.NexusArchiveWriter;
import nexusvault.archive.impl.IndexFile.IndexDirectoryData;

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

	private void processDirectory(TreeDirectory directory) throws IOException {
		for (final TreeDirectory subdir : directory.getDirectories()) {
			if (subdir.isWaitingForChildWriteUpdate() || subdir.isWaitingForDirectoryUpdate()) {
				processDirectory(subdir);
			}

			if (subdir.getDirectoryIndex() == -1) {
				throw new IllegalStateException();
			}
		}

		if (isDirectoryNew(directory) || isADirectoryChildNew(directory) || hasMoreThanNFilesChanged(directory, 1)) {

			indexFile.enableWriteMode();
			archiveFile.enableWriteMode();

			final List<IdxDirectoryAttribute> directories = new ArrayList<>();
			final List<IdxFileAttribute> files = new ArrayList<>();

			for (final TreeDirectory subdir : directory.getDirectories()) {
				final var attribute = new IdxDirectoryAttribute(subdir.getName(), subdir.getDirectoryIndex());
				directories.add(attribute);
			}

			for (final TreeFile file : directory.getFiles()) {

				IdxFileAttribute fileAttribute;
				if (file.hasFlags(TreeEntry.FLAG_NEW)) {
					fileAttribute = writeToArchive(null, file.getFileData());
					fileAttribute.setName(file.getName());
				} else if (file.hasFlags(TreeEntry.FLAG_NEW_DATA)) {
					fileAttribute = file.getFileAttribute();
					if (fileAttribute == null) {
						throw new IllegalStateException(); // TODO
					}
					fileAttribute = writeToArchive(fileAttribute.getHash(), file.getFileData());
					fileAttribute.setName(file.getName());
				} else {
					fileAttribute = file.getFileAttribute();
					if (fileAttribute == null) {
						throw new IllegalStateException(); // TODO
					}
				}

				file.setFileAttribute(fileAttribute);
				files.add(fileAttribute);
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
					var fileAttribute = file.getFileAttribute();
					fileAttribute = writeToArchive(fileAttribute.getHash(), file.getFileData());
					indexFile.overwriteFileAttribute(directory.getDirectoryIndex(), index, fileAttribute.getHash(), fileAttribute);
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

	private boolean isADirectoryChildNew(TreeDirectory directory) {
		for (final var child : directory.getDirectories()) {
			if (child.hasFlags(TreeEntry.FLAG_NEW)) {
				return true;
			}
		}
		for (final var child : directory.getFiles()) {
			if (child.hasFlags(TreeEntry.FLAG_NEW)) {
				return true;
			}
		}
		return false;
	}

	private boolean isDirectoryNew(TreeDirectory directory) {
		return directory.hasFlags(TreeEntry.FLAG_NEW);
	}

	private IdxFileAttribute writeToArchive(byte[] oldHash, DataSource data) throws IOException {
		final BinaryReader uncompressedData = data.getData();
		final DataSourceConfig config = data.getConfig();
		final BinaryReader compressedData = compress(uncompressedData, config.getRequestedCompressionType());

		final long uncompressedSize = uncompressedData.size();
		final long compressedSize = compressedData.size();
		final byte[] hash = computeHash(compressedData);

		if (oldHash == null) {
			archiveFile.writeArchiveData(hash, compressedData);
		} else {
			archiveFile.replaceArchiveData(oldHash, hash, compressedData);
		}

		final int flags = config.getRequestedCompressionType().getFlag();
		return new IdxFileAttribute("", flags, System.currentTimeMillis(), uncompressedSize, compressedSize, hash, 0);
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
	public void write(DataSource source, IdxPath destination) throws IOException {
		completeFileTree(source, destination);
	}

	public void replace(IdxPath src, IdxPath destination, boolean deleteOldDestination) {

	}

	private void completeFileTree(DataSource source, IdxPath destination) {
		if (source == null) {
			throw new IllegalArgumentException("'source' must not be null");
		}

		if (destination == null) {
			throw new IllegalArgumentException("'destination' must not be null");
		}

		final TreeFile file = findOrCreateFile(destination);
		file.setFileData(source);
	}

	private TreeFile findOrCreateFile(IdxPath destination) {
		final Iterator<String> iterator = destination.iterateElements().iterator();
		return findOrCreateFile(iterator);
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

		// TODO not so happy
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

		public TreeDirectory(TreeDirectory parent, IdxDirectoryAttribute directoryAttribute) {
			this(parent, directoryAttribute.getName(), directoryAttribute.getDirectoryIndex());
		}

		/**
		 * @return the directory index, or <code>-1</code> if no directory index is set
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
			setFlags(FLAG_CHILD_CHANGED);
			subdir.setFlags(FLAG_ADDED);
			subdir.notifyParentAboutPendingWriteUpdate();
		}

		protected void addFile(TreeFile file) {
			files.add(file);
			setFlags(FLAG_CHILD_CHANGED);
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
		private IdxFileAttribute fileAttribute;
		private DataSource data;

		public TreeFile(TreeDirectory parent, IdxFileAttribute fileAttribute) {
			super(parent, fileAttribute.getName());
			this.fileAttribute = fileAttribute;
		}

		public TreeFile(TreeDirectory parent, String name) {
			super(parent, name);
			fileAttribute = null;
		}

		protected void setFileAttribute(IdxFileAttribute info) {
			fileAttribute = info;
		}

		public IdxFileAttribute getFileAttribute() {
			return fileAttribute;
		}

		public void setFileData(DataSource data) {
			this.data = data;
			setFlags(FLAG_NEW_DATA);
			notifyParentAboutPendingWriteUpdate();
			getParent().setFlags(FLAG_CHILD_CHANGED);
		}

		public DataSource getFileData() {
			return data;
		}
	}

}
