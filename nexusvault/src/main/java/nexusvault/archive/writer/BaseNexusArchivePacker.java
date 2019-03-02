package nexusvault.archive.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.io.util.Seek;
import kreed.io.util.SeekableByteChannelBinaryWriter;
import kreed.reflection.struct.StructUtil;
import nexusvault.archive.IdxEntryNotADirectoryException;
import nexusvault.archive.IdxEntryNotAFileException;
import nexusvault.archive.IdxPath;
import nexusvault.archive.struct.StructAIDX;
import nexusvault.archive.struct.StructArchiveEntry;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructPackHeader;

public final class BaseNexusArchivePacker implements NexusArchiveWriter {

	private static final int SIZE_2_MB = 2 << 20;

	private static abstract class TreeEntry {
		private final String name;

		protected TreeEntry(String name) {
			this.name = name;
		}

		final public String getName() {
			return name;
		}

	}

	private static final class TreeDirectory extends TreeEntry {

		private final List<TreeDirectory> subDirs;
		private final List<TreeFile> files;
		private int packIdx;

		public TreeDirectory(String name) {
			super(name);
			this.subDirs = new LinkedList<>();
			this.files = new LinkedList<>();
		}

		public TreeFile createFile(IdxPath path) {
			return createFile(path.iterateElements().iterator());
		}

		public TreeFile createFile(Iterator<String> path) {
			TreeDirectory dir = this;
			while (path.hasNext()) {
				final String entryName = path.next();
				final TreeEntry entry = dir.getEntryAndValidateName(entryName);
				if (!path.hasNext()) {
					// this is the last element of the path.
					if (entry == null) {
						return dir.createFile(entryName);
					} else if (entry instanceof TreeFile) {
						return (TreeFile) entry;
					} else {
						// In this case there is a directory with the same name as a file
						throw new IdxEntryNotAFileException(entryName);
					}
				} else {
					// this is not the last element of the path
					if (entry == null) {
						dir = dir.createDirectory(entryName);
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

		private TreeEntry getEntry(String entryName) {
			for (final TreeEntry entry : subDirs) {
				if (entry.getName().equalsIgnoreCase(entryName)) {
					return entry;
				}
			}

			for (final TreeEntry entry : files) {
				if (entry.getName().equalsIgnoreCase(entryName)) {
					return entry;
				}
			}
			return null;
		}

		private TreeEntry getEntryAndValidateName(String entryName) {
			final TreeEntry entry = getEntry(entryName);
			if ((entry != null) && !entry.getName().equals(entryName)) {
				throw new IllegalStateException(entryName); // TODO
			}
			return entry;
		}

		public TreeDirectory createDirectory(String directoryName) {
			final TreeDirectory dir = new TreeDirectory(directoryName);
			subDirs.add(dir);
			return dir;
		}

		public TreeFile createFile(String fileName) {
			final TreeFile file = new TreeFile(fileName);
			files.add(file);
			return file;
		}

		public int getDirectoryCount() {
			return subDirs.size();
		}

		public int getFileCount() {
			return files.size();
		}

		public List<TreeDirectory> getDirectories() {
			return subDirs;
		}

		public List<TreeFile> getFiles() {
			return files;
		}

		public void setPackIdx(int packIdx) {
			this.packIdx = packIdx;
		}

		public int getPackIdx() {
			return packIdx;
		}

		public int countAllDirectories() {
			return 1 + subDirs.stream().mapToInt(TreeDirectory::countAllDirectories).sum();
		}

		public int countAllFiles() {
			return files.size() + subDirs.stream().mapToInt(TreeDirectory::countAllFiles).sum();
		}

	}

	private static final class TreeFile extends TreeEntry {

		private IntegrateableElement element;

		public TreeFile(String name) {
			super(name);
		}

		public void setElement(IntegrateableElement element) {
			this.element = element;
		}

		public IntegrateableElement getElement() {
			return element;
		}

	}

	private Path indexPath;
	private Path archivePath;

	private BinaryWriter indexFile;
	private BinaryWriter archiveFile;

	private long indexPackPosition;
	private int indexPackCount;

	private long archivePackPosition;
	private int archivePackCount;
	private long archiveEntryPosition;
	private int archiveEntryCount;

	@Override
	public void packAll(List<IntegrateableElement> src) throws IOException {
		src = sortData(src);
		verifyData(src);
		final TreeDirectory fileTree = buildFileTree(src);

		write(fileTree);
	}

	private List<IntegrateableElement> sortData(List<IntegrateableElement> src) {
		final ArrayList<IntegrateableElement> sorted = new ArrayList<>(src);
		sorted.sort(new Comparator<IntegrateableElement>() {
			@Override
			public int compare(IntegrateableElement o1, IntegrateableElement o2) {
				return o1.getDestination().compareTo(o2.getDestination());
			}
		});
		return sorted;
	}

	private void verifyData(List<IntegrateableElement> src) {
		// TODO Auto-generated method stub

	}

	private TreeDirectory buildFileTree(List<IntegrateableElement> elements) {
		final TreeDirectory root = new TreeDirectory("");
		for (final IntegrateableElement element : elements) {
			final TreeFile file = root.createFile(element.getDestination());
			file.setElement(element);
		}
		return root;
	}

	private BinaryWriter openWriter(Path path, int bufferSize) throws IOException {
		final SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING);
		return new SeekableByteChannelBinaryWriter(channel, ByteBuffer.allocate(bufferSize));
	}

	private void write(TreeDirectory fileTree) throws IOException {
		try (BinaryWriter indexFile = openWriter(this.indexPath, SIZE_2_MB); BinaryWriter archiveFile = openWriter(this.archivePath, SIZE_2_MB)) {
			this.indexFile = indexFile;
			this.archiveFile = archiveFile;

			initializeIndex(fileTree);
			initializeArchive();
			processFileTree(fileTree);
			finalizeIndex();
			finalizeArchive();
		} finally {
			this.indexFile = null;
			this.archiveFile = null;
		}
	}

	private void initializeIndex(TreeDirectory fileTree) {
		reserveIndexHeaderSpace();
		reserveIndexPackSpace(fileTree);
		writeIndexInitialPack(fileTree);
	}

	private void reserveIndexHeaderSpace() {
		this.indexFile.seek(Seek.BEGIN, StructArchiveFile.SIZE_IN_BYTES);
		this.indexFile.writeInt64(0); // guard stop
	}

	private void reserveIndexPackSpace(TreeDirectory fileTree) {
		final int numberOfDirectories = fileTree.countAllDirectories();
		final int expectedPacks = numberOfDirectories + 1 /* aidx */ + 1 /* empty */;
		final long packBlockSize = (expectedPacks + 300 /* some air */) * StructPackHeader.SIZE_IN_BYTES;

		this.indexFile.writeInt64(packBlockSize); // pack guard
		this.indexPackPosition = this.indexFile.getPosition();
		this.indexFile.seek(Seek.CURRENT, packBlockSize);
		this.indexFile.writeInt64(packBlockSize); // pack guard
	}

	private void writeIndexInitialPack(TreeDirectory fileTree) {
		// first 3 packs are reserved
		// 0 -> empty
		// 1 -> root directory
		// 2 -> AIDX
		this.indexPackCount = 3;
		fileTree.setPackIdx(1);

		// write AIDX && base packs
		final long aidxOffset = writeAIDX(new StructAIDX(StructAIDX.SIGNATURE_AIDX, 1, 16042, 1));
		writeIndexPack(0, new StructPackHeader()); // empty pack
		writeIndexPack(2, new StructPackHeader(aidxOffset, StructAIDX.SIZE_IN_BYTES)); // points at aidx
	}

	private long writeAIDX(StructAIDX aidx) {
		this.indexFile.writeInt64(StructAIDX.SIZE_IN_BYTES);
		final long offset = this.indexFile.getPosition();
		this.indexFile.writeInt32(aidx.signature);
		this.indexFile.writeInt32(aidx.version);
		this.indexFile.writeInt32(aidx.entryCount);
		this.indexFile.writeInt32(aidx.headerIdx);
		this.indexFile.writeInt64(StructAIDX.SIZE_IN_BYTES);
		return offset;
	}

	private void finalizeIndex() {
		writeIndexHeader();
	}

	private void writeIndexHeader() {
		this.indexFile.seek(Seek.BEGIN, 0);
		final StructArchiveFile header = StructUtil.buildStruct(StructArchiveFile.class);
		header.signature = StructArchiveFile.FILE_SIGNATURE;
		header.version = 1;
		// header.unknown_008 = new byte[512];
		header.fileSize = this.indexFile.size();
		header.packOffset = this.indexPackPosition;
		header.packCount = this.indexPackCount;
		header.packRootIdx = 2;
		StructUtil.writeStruct(header, this.indexFile, true);
	}

	private void initializeArchive() {
		// TODO Auto-generated method stub

	}

	private void finalizeArchive() {
		// TODO Auto-generated method stub

	}

	private void processFileTree(TreeDirectory fileTree) {
		final Queue<TreeDirectory> queue = new LinkedList<>();
		queue.add(fileTree);

		while (!queue.isEmpty()) {
			final TreeDirectory directory = queue.poll();
			moveIndexToEnd();
			writeIndexDirectory(directory);
			queue.addAll(directory.getDirectories());
		}
	}

	private void moveIndexToEnd() {
		this.indexFile.seek(Seek.BEGIN, this.indexFile.size());
	}

	private void writeIndexDirectory(TreeDirectory directory) {

		this.indexFile.writeInt64(0); // block guard placeholder
		final long blockStart = this.indexFile.getPosition();

		if (padPosition(blockStart) != blockStart) {
			throw new IllegalStateException(); // TODO Improve exception. If this happens, something did go really wrong
		}

		this.indexFile.writeInt32(directory.getDirectoryCount());
		this.indexFile.writeInt32(directory.getFileCount());

		int nameOffset = 0;

		for (final TreeDirectory subdir : directory.getDirectories()) {
			subdir.setPackIdx(this.indexPackCount);
			this.indexPackCount += 1;

			this.indexFile.writeInt32(nameOffset);
			this.indexFile.writeInt32(subdir.getPackIdx());

			final String name = subdir.getName();
			final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
			nameOffset += nameBytes.length + 1;
		}

		for (final TreeFile file : directory.getFiles()) {
			final ArchiveEntryInfo info = writeArchiveData(file.getElement());
			// file.getArchiveInfo();
			this.indexFile.writeInt32(nameOffset);
			this.indexFile.writeInt32(info.getFlags());
			this.indexFile.writeInt64(info.getWriteTime());
			this.indexFile.writeInt64(info.getUncompressedSize());
			this.indexFile.writeInt64(info.getCompressedSize());
			this.indexFile.writeInt8From(info.getHash(), 0, 20);

			final String name = file.getName();
			final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
			nameOffset += nameBytes.length + 1;
		}

		writeIndexNames(directory.getDirectories());
		writeIndexNames(directory.getFiles());

		final long blockEnd = this.indexFile.getPosition();
		final long paddedBlockEnd = padPosition(blockEnd);
		final long blockSize = blockEnd - blockStart;
		final long paddedBlockSize = paddedBlockEnd - blockStart;

		// write correct block guards
		this.indexFile.seek(Seek.BEGIN, blockStart - 8);
		this.indexFile.writeInt64(paddedBlockSize);
		this.indexFile.seek(Seek.BEGIN, paddedBlockEnd);
		this.indexFile.writeInt64(paddedBlockSize);

		writeIndexPack(directory.getPackIdx(), new StructPackHeader(blockStart, blockSize));
	}

	private void writeIndexNames(List<? extends TreeEntry> entries) {
		for (final TreeEntry entry : entries) {
			final String name = entry.getName();
			final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
			if (name.length() != nameBytes.length) {
				throw new IllegalStateException(); // TODO Shit hits the fan again
			}
			this.indexFile.writeInt8From(nameBytes, 0, nameBytes.length);
			this.indexFile.writeInt8(0); // 0 terminated
		}
	}

	private void writeIndexPack(int packIdx, StructPackHeader structPackHeader) {
		this.indexFile.seek(Seek.BEGIN, this.indexPackPosition + (packIdx * StructPackHeader.SIZE_IN_BYTES));
		this.indexFile.writeInt64(structPackHeader.offset);
		this.indexFile.writeInt64(structPackHeader.size);
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

	/**
	 * Appends the given data to the end of the archive file. <br>
	 *
	 * @param data
	 *            to store in the archive
	 * @return offset at which the data can be found.
	 */
	private long writeArchiveData(BinaryReader data) {
		final long blockStartGuard = this.archiveFile.size();
		final long blockStart = blockStartGuard + 8;
		final long blockEndGuard = padPosition(blockStart + data.size());
		final long blockSize = blockEndGuard - blockStart;

		// TODO Assert offsets

		this.archiveFile.writeInt64(blockSize); // start block guard
		this.archiveFile.write(data);
		this.archiveFile.seek(Seek.BEGIN, blockEndGuard);
		this.archiveFile.writeInt64(blockSize); // end block guard

		// TODO Assert offsets

		return blockStart;
	}

	private long writeArchivePack(StructPackHeader structPackHeader) {
		this.archiveFile.seek(Seek.BEGIN, this.archivePackPosition + (this.archivePackCount * StructPackHeader.SIZE_IN_BYTES));
		this.archiveFile.writeInt64(structPackHeader.offset);
		this.archiveFile.writeInt64(structPackHeader.size);
		final int idx = this.archivePackCount;
		this.archivePackCount += 1;
		return idx;
	}

	private void writeArchiveEntry(StructArchiveEntry entry) {
		this.archiveFile.seek(Seek.BEGIN, this.archiveEntryPosition + (this.archiveEntryCount * StructArchiveEntry.SIZE_IN_BYTES));
		this.archiveFile.writeInt32(entry.headerIdx);
		this.archiveFile.writeInt8From(entry.hash, 0, entry.hash.length);
		this.archiveFile.writeInt64(entry.size);
		this.archiveEntryCount += 1;
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

}
