package nexusvault.archive.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import kreed.io.util.SeekableByteChannelBinaryReader;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotAFileException;
import nexusvault.archive.IdxPath;
import nexusvault.archive.impl.ArchivePathLocator;
import nexusvault.archive.impl.IndexFile;
import nexusvault.archive.impl.IndexFile.IndexDirectoryData;
import nexusvault.archive.impl.IndexFileReader;
import nexusvault.archive.struct.StructArchiveEntry;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.writer.NexusArchiveWriter.CompressionType;
import nexusvault.archive.writer.NexusArchiveWriter.IntegrateableElement;
import nexusvault.archive.writer.NexusArchiveWriter.IntegrateableElementConfig;

final class BaseNexusArchiveModifier implements AutoCloseable {

	private static final int SIZE_2_MB = 2 << 20;

	public static void main(String[] a) throws IOException {
		final Path p = Paths.get("test.bin");
		System.out.println(p.toAbsolutePath());
		final SeekableByteChannel c = Files.newByteChannel(p, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
		final ByteBuffer b = ByteBuffer.allocate(1);

		System.out.println(c.size());
		while (c.position() != c.size()) {
			b.clear();
			c.read(b);
			b.flip();
			final byte data = b.get();
			if (data == 0) {
				c.truncate(c.position() - 1);
			} else if ((data % 4) == 0) {
				c.position(c.position() - 1);
				b.clear();
				b.put((byte) 0xFF);
				b.flip();
				c.write(b);
			}
		}

		c.close();
	}

	private static enum UpdateFlag {
		NEW_CHILD,
		FILE_DATA,
		FILE_ATTRIBUTE,
		NAME,
		CREATED,
		REMOVE,
		UPDATE_CHILD
	}

	private static abstract class TreeEntry {

		private final String name;
		private final TreeDirectory parent;
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

		protected void notifyParentAboutUpdate() {
			if (parent != null) {
				if (!parent.hasFlag(UpdateFlag.UPDATE_CHILD)) {
					parent.setFlag(UpdateFlag.UPDATE_CHILD);
					parent.notifyParentAboutUpdate();
				}
			}
		}

		protected final void setFlag(UpdateFlag flag) {
			this.flags |= (1 << flag.ordinal());
		}

		protected final void unsetFlag(UpdateFlag flag) {
			this.flags &= ~(1 << flag.ordinal());
		}

		protected final boolean hasFlag(UpdateFlag flag) {
			return (flags & (1 << flag.ordinal())) != 0;
		}

		protected final boolean isEntryFlaggedForUpdate() {
			return hasOtherFlagsBeside(UpdateFlag.UPDATE_CHILD);
		}

		private final boolean hasOtherFlagsBeside(UpdateFlag flag) {
			return (flags & ~(1 << flag.ordinal())) != 0;
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

		public TreeDirectory(TreeDirectory parent, StructIdxDirectory directory) {
			this(parent, directory.name, directory.directoryHeaderIdx);
		}

		public int getPackIdx() {
			return packIdx;
		}

		protected boolean hasChildWithPendingUpdate() {
			return this.hasFlag(UpdateFlag.UPDATE_CHILD);
		}

		protected void addDirectory(TreeDirectory subdir) {
			directories.add(subdir);
			setFlag(UpdateFlag.NEW_CHILD);
			notifyParentAboutUpdate();
		}

		protected void addFile(TreeFile file) {
			files.add(file);
			setFlag(UpdateFlag.NEW_CHILD);
			notifyParentAboutUpdate();
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
			setFlag(UpdateFlag.FILE_DATA);
			notifyParentAboutUpdate();
		}

		public IntegrateableElement getElement() {
			return element;
		}
	}

	private Path indexPath;
	private Path archivePath;

	private SeekableByteChannel indexFile;
	private SeekableByteChannel archiveFile;
	private ByteBuffer buffer;

	private TreeDirectory root;
	private long indexPackPosition;
	private int indexPackCount;

	private long archivePackPosition;
	private int archivePackCount;
	private long archiveEntryPosition;
	private int archiveEntryCount;

	public void open(Path indexOrArchive) throws IOException {
		close();

		indexPath = ArchivePathLocator.getIndexPath(indexOrArchive);
		archivePath = ArchivePathLocator.getArchivePath(indexOrArchive);

		buildIndexTree();
	}

	@Override
	public void close() throws IOException {
		root = null;
		// TODO
	}

	private void buildIndexTree() throws IOException {
		if (!Files.exists(indexPath)) {
			this.root = new TreeDirectory(null, "", 1);
			this.indexPackCount = 2;
		}

		try (BinaryReader reader = new SeekableByteChannelBinaryReader(openFileAccess(indexPath), buffer)) {
			final IndexFile indexFile = new IndexFileReader().read(reader);
			final TreeDirectory root = new TreeDirectory(null, "", indexFile.getPackRootIdx());
			buildIndexTree(root, indexFile);

			this.root = root;
			this.indexPackCount = indexFile.getPackCount();

			indexFile.dispose();
		}
	}

	private void buildIndexTree(TreeDirectory root, IndexFile indexFile) throws IOException {
		final Queue<TreeDirectory> queue = new LinkedList<>();
		queue.add(root);

		while (!queue.isEmpty()) {
			final TreeDirectory dir = queue.poll();
			final IndexDirectoryData directoryData = indexFile.getDirectoryData(dir.getPackIdx());
			directoryData.getDirectories().stream().map(d -> new TreeDirectory(dir, d)).forEach(dir.directories::add);
			directoryData.getFileLinks().stream().map(f -> new TreeFile(dir, f)).forEach(dir.files::add);
			queue.addAll(dir.getDirectories());
		}
	}

	private SeekableByteChannel openFileAccess(Path path) throws IOException {
		final SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
		return channel;
	}

	public void setData(List<IntegrateableElement> src) throws IOException {
		verifyData(src);
		for (final IntegrateableElement element : src) {
			final TreeFile file = createNewFile(root, element.getDestination());
			file.setElement(element);
		}
	}

	private TreeFile createNewFile(TreeDirectory root, IdxPath path) {
		return createNewFile(root, path.iterateElements().iterator());
	}

	public void updateArchive() throws IOException {
		try (SeekableByteChannel indexFile = openFileAccess(indexPath); SeekableByteChannel archiveFile = openFileAccess(archivePath)) {
			this.indexFile = indexFile;
			this.archiveFile = archiveFile;
			this.buffer = ByteBuffer.allocate(SIZE_2_MB);

			final Queue<TreeDirectory> queue = new LinkedList<>();
			queue.add(root);

			while (!queue.isEmpty()) {
				final TreeDirectory directory = queue.poll();
				directory.getDirectories().stream().filter(TreeDirectory::hasChildWithPendingUpdate).forEach(queue::add);
				directory.getDirectories().stream().filter(TreeEntry::isEntryFlaggedForUpdate).forEach(child -> updateDirectory(directory, child));
				directory.getFiles().stream().filter(TreeEntry::isEntryFlaggedForUpdate).forEach(child -> updateFile(directory, child));
				directory.clearFlags();
			}
		} finally {
			this.indexFile = null;
			this.archiveFile = null;
			this.buffer = null;
		}
	}

	private void updateDirectory(TreeDirectory parent, TreeDirectory child) {
		// TODO Auto-generated method stub
	}

	private void updateFile(TreeDirectory parent, TreeFile child) {
		if (child.hasFlag(UpdateFlag.FILE_DATA)) {
			// step 1: compress new data
			// step 2: create archive info
			// step 3: check is file new
			// ----> No:
			// ---- step 3.1: search archive for old file hash
			// ---- step 3.2: find archive entry, referenced pack and data block
			// ---- step 3.3: check is data block big enough for the new data
			// ---- ----> No:
			// ---- ---- step 3.3.1: mark block as free
			// ---- ---- step 3.3.2: check is next block free
			// ---- ---- ----> Yes:
			// ---- ---- ---- step 3.3.2.1: Combine block & next block
			// ---- ---- ---- step 3.3.2.2: return to 3.3
			// ---- ---- ----> No:
			// ---- ---- ---- step 3.3.2.1: scan archive for free block big enough
			// ---- ---- ---- step 3.3.2.2: Check block found
			// ---- ---- ---- ----> Yes:
			// ---- ---- ---- ---- ---- step 3.3.2.2.1: return to 3.3
			// ---- ---- ---- ----> No:
			// ---- ---- ---- ---- ---- step 3.3.2.2.1: Create block at end of archive or swap/shift blocks to make space
			// ---- ---- ---- ---- ---- step 3.3.2.2.2: return to 3.3
			// ---- ----> Yes:
			// ---- step 3.4: write data to block
			// ---- step 3.5: mark remaining space as free
			// ---- step 3.6: update pack & archive entry
		}
		// TODO Auto-generated method stub
	}

	private void scanArchiveFileForEmptyBlocks(BinaryReader reader, boolean direction) {
		while (!reader.isEndOfData()) {
			final long size = reader.readInt64();
			if (size == 0) { // end of archive
				break;
			} else if (size < 0) {
				// TODO mark free block
			}
			final long move = Math.abs(size) + Long.BYTES;
			reader.seek(Seek.CURRENT, direction ? move : -move);
		}
	}

	private ArchiveEntryInfo writeArchiveData(IntegrateableElement element) {
		final ArchiveEntryInfo archiveInfo = new ArchiveEntryInfo();

		final BinaryReader originalData = element.getData();
		final IntegrateableElementConfig config = element.getConfig();
		final BinaryReader compressedData = compress(originalData, config.getCompressionType());

		final long uncompressedSize = originalData.size();
		final long compressedSize = compressedData.size();
		final byte[] hash = calculateHash(compressedData);
		compressedData.seek(Seek.BEGIN, 0);

		archiveInfo.setFlags(config.getCompressionType().getFlag());
		archiveInfo.setWriteTime(System.currentTimeMillis());
		archiveInfo.setUncompressedSize(uncompressedSize);
		archiveInfo.setCompressedSize(compressedSize);
		archiveInfo.setHash(hash);

		final long dataOffset = writeArchiveData(compressedData);
		final long packIdx = writeArchivePack(new StructPackHeader(dataOffset, compressedSize));
		writeArchiveEntry(new StructArchiveEntry(packIdx, hash, uncompressedSize));

		return archiveInfo;
	}

	private void writeArchiveEntry(StructArchiveEntry structArchiveEntry) {
		// TODO Auto-generated method stub

	}

	private long writeArchivePack(StructPackHeader structPackHeader) {
		// TODO Auto-generated method stub
		return 0;
	}

	private long writeArchiveData(BinaryReader compressedData) {
		// TODO Auto-generated method stub
		return 0;
	}

	private BinaryReader compress(BinaryReader originalData, CompressionType compressionType) {
		// TODO Auto-generated method stub
		return null;
	}

	private byte[] calculateHash(BinaryReader data) {
		// TODO Auto-generated method stub
		return null;
	}

	private static long padPosition(long pos) {
		return (pos + 0xF) & 0xFFFFFFFFFFFFFFF0l;
	}

	private TreeEntry getEntryAndValidateName(TreeDirectory directory, String entryName) {
		final TreeEntry entry = directory.getEntry(entryName);
		if ((entry != null) && !entry.getName().equals(entryName)) {
			throw new IllegalStateException(entryName); // TODO
		}
		return entry;
	}

	private TreeFile createNewFile(TreeDirectory root, Iterator<String> path) {
		TreeDirectory dir = root;
		while (path.hasNext()) {
			final String entryName = path.next();
			final TreeEntry entry = getEntryAndValidateName(dir, entryName);
			if (!path.hasNext()) {
				// this is the last element of the path.
				if (entry == null) {
					final TreeFile newFile = new TreeFile(dir, entryName);
					newFile.setFlag(UpdateFlag.CREATED);
					newFile.notifyParentAboutUpdate();
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
					final TreeDirectory newDir = new TreeDirectory(dir, entryName, indexPackCount++);
					newDir.setFlag(UpdateFlag.CREATED);
					newDir.notifyParentAboutUpdate();
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
		return null;
	}

	private void verifyData(List<IntegrateableElement> src) {
		// TODO Auto-generated method stub
	}

}
