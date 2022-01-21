package nexusvault.vault;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kreed.io.util.BinaryIOException;
import nexusvault.vault.IdxEntry.IdxDirectory;
import nexusvault.vault.IdxEntry.IdxFileLink;
import nexusvault.vault.archive.Hash;
import nexusvault.vault.archive.PackedArchiveFile;
import nexusvault.vault.codec.LzmaCodec;
import nexusvault.vault.codec.ZipCodec;
import nexusvault.vault.index.IndexException.IndexEntryNotADirectoryException;
import nexusvault.vault.index.IndexException.IndexEntryNotAFileException;
import nexusvault.vault.index.IndexException.IndexEntryNotFoundException;
import nexusvault.vault.index.Node;
import nexusvault.vault.index.Node.DirectoryNode;
import nexusvault.vault.index.Node.FileNode;
import nexusvault.vault.index.PackedIndexFile;
import nexusvault.vault.util.ArchivePathLocator;

final class NexusArchiveImpl implements NexusArchive {

	private static abstract class IdxEntryImpl<T extends Node> implements IdxEntry {

		protected final T node;
		protected final NexusArchiveImpl archive;

		public IdxEntryImpl(NexusArchiveImpl archive, T node) {
			this.archive = archive;
			this.node = node;
		}

		final protected IdxEntry decorate(Node node) {
			if (node.isDirectory()) {
				return new IdxDirectoryImpl(this.archive, node.asDirectory());
			}
			return new IdxFileLinkImpl(this.archive, node.asFile());
		}

		@Override
		final public IdxFileLink asFile() throws IndexEntryNotAFileException {
			if (isFile()) {
				return (IdxFileLinkImpl) this;
			} else {
				throw new IndexEntryNotAFileException(getFullName());
			}
		}

		@Override
		final public IdxDirectory asDirectory() throws IndexEntryNotADirectoryException {
			if (isDirectory()) {
				return (IdxDirectoryImpl) this;
			} else {
				throw new IndexEntryNotADirectoryException(getFullName());
			}
		}

		@Override
		final public NexusArchiveImpl getArchive() {
			return this.archive;
		}

		@Override
		final public String getName() {
			return this.node.getName();
		}

		@Override
		final public String getFullName() {
			return getPath().getFullName();
		}

		@Override
		public IdxDirectoryImpl getParent() {
			final var parent = this.node.getParent();
			if (this.node == null) {
				return null;
			}
			return new IdxDirectoryImpl(this.archive, parent);
		}

		@Override
		public IdxPath getPath() {
			return this.node.toPath();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}

			if (!(obj instanceof IdxEntryImpl)) {
				return false;
			}

			final var otherEntry = (IdxEntryImpl) obj;
			return otherEntry.getArchive() == getArchive() && otherEntry.node == this.node;
		}

	}

	private static final class IdxDirectoryImpl extends IdxEntryImpl<DirectoryNode> implements IdxDirectory {

		public IdxDirectoryImpl(NexusArchiveImpl archive, DirectoryNode directory) {
			super(archive, directory);
		}

		@Override
		public boolean isFile() {
			return false;
		}

		@Override
		public boolean isDirectory() {
			return true;
		}

		@Override
		public List<IdxEntry> getEntries() {
			return this.node.getChilds().stream() //
					.map(this::decorate) //
					.collect(Collectors.toList());
		}

		public List<IdxEntry> getChildsDeep() {
			final List<IdxEntry> results = new LinkedList<>();
			final Deque<IdxDirectory> fringe = new LinkedList<>();
			fringe.add(this);
			while (!fringe.isEmpty()) {
				final IdxDirectory dir = fringe.pollFirst();
				results.add(dir);
				for (final IdxEntry child : dir.getEntries()) {
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
		public int countNodesInSubTree() {
			return this.node.countNodesInSubTree();
		}

		@Override
		public List<IdxDirectory> getDirectories() {
			return this.node.getDirectories().stream().map(node -> new IdxDirectoryImpl(getArchive(), node)).collect(Collectors.toList());
		}

		@Override
		public List<IdxFileLink> getFiles() {
			return this.node.getFiles().stream().map(node -> new IdxFileLinkImpl(getArchive(), node)).collect(Collectors.toList());
		}

		@Override
		public boolean hasEntry(String entryName) {
			return this.node.getChild(entryName).isPresent();
		}

		@Override
		public Optional<IdxEntry> getEntry(String entryName) {
			final var optional = this.node.getChild(entryName);
			if (optional.isPresent()) {
				return Optional.of(decorate(optional.get()));
			}
			return Optional.empty();
		}

		@Override
		public IdxDirectory getDirectory(String directoryName) throws IndexEntryNotFoundException, IndexEntryNotADirectoryException {
			return getEntry(directoryName).orElseThrow(() -> new IndexEntryNotFoundException(directoryName)).asDirectory();
		}

		@Override
		public IdxFileLink getFileLink(String fileLinkName) throws IndexEntryNotFoundException, IndexEntryNotAFileException {
			return getEntry(fileLinkName).orElseThrow(() -> new IndexEntryNotFoundException(fileLinkName)).asFile();
		}

		@Override
		public Optional<IdxEntry> find(IdxPath path) {
			final var node = this.node.find(path);
			if (node.isPresent()) {
				return Optional.of(decorate(node.get()));
			}
			return Optional.empty();
		}

	}

	private static final class IdxFileLinkImpl extends IdxEntryImpl<FileNode> implements IdxFileLink {

		public IdxFileLinkImpl(NexusArchiveImpl archive, FileNode file) {
			super(archive, file);
		}

		@Override
		public boolean isFile() {
			return true;
		}

		@Override
		public boolean isDirectory() {
			return false;
		}

		@Override
		public byte[] getHash() {
			return this.node.getHash();
		}

		@Override
		public int getFlags() {
			return this.node.getFlags();
		}

		@Override
		public long getUncompressedSize() {
			return this.node.getUncompressedSize();
		}

		@Override
		public long getCompressedSize() {
			return this.node.getCompressedSize();
		}

		@Override
		public long getWriteTime() {
			return this.node.getWriteTime();
		}

		@Override
		public String getFileEnding() {
			final int fileExtStart = getName().lastIndexOf(".");
			if (fileExtStart < 0) {
				return "";
			} else {
				return getName().substring(fileExtStart + 1);
			}
		}

		@Override
		public String getNameWithoutFileExtension() {
			final String name = getName();
			final int ext = name.lastIndexOf('.');
			if (ext < 0) {
				return name;
			} else {
				return name.substring(0, ext);
			}
		}

		@Override
		public byte[] getData() throws IOException {
			return getArchive().getData(this);
		}
	}

	private final Object synchronizationLock = new Object();

	private final PackedIndexFile indexFile = new PackedIndexFile();
	private final PackedArchiveFile archiveFile = new PackedArchiveFile();
	private NexusArchiveFiles files;
	private boolean isDisposed = true;

	private IdxDirectory rootDirectory;

	public NexusArchiveImpl(Path archiveOrIndex) throws IOException {
		this();
		load(archiveOrIndex);
	}

	public NexusArchiveImpl() {
	}

	@Override
	public void load(Path archiveOrIndex) throws IOException {
		final var idxPath = ArchivePathLocator.getIndexPath(archiveOrIndex);
		final var arcPath = ArchivePathLocator.getArchivePath(archiveOrIndex);
		load(idxPath, arcPath);
	}

	@Override
	public void reload() throws IOException {
		final var files = getFiles();
		load(files.getIndexFile(), files.getArchiveFile());
	}

	private void load(Path idxPath, Path arcPath) throws IOException {
		synchronized (this.synchronizationLock) {
			dispose();
			this.files = new NexusArchiveFiles(idxPath, arcPath);
			this.indexFile.open(idxPath);
			this.archiveFile.open(arcPath);
			this.isDisposed = false;
			this.rootDirectory = new IdxDirectoryImpl(this, this.indexFile.getRoot());
		}
	}

	@Override
	public void dispose() {
		synchronized (this.synchronizationLock) {
			if (this.isDisposed) {
				return;
			}
			this.isDisposed = true;

			try {
				this.indexFile.close();
				this.archiveFile.close();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO
		}
	}

	@Override
	public boolean isDisposed() {
		return this.isDisposed;
	}

	@Override
	public NexusArchiveFiles getFiles() {
		return this.files;
	}

	@Override
	public IdxDirectory getRootDirectory() {
		return this.rootDirectory;
	}

	@Override
	public int getNumberOfFiles() {
		try {
			return this.archiveFile.getNumberOfEntries();
		} catch (final IOException e) {
			throw new VaultException(e); // TODO
		}
	}

	@Override
	public void write(IdxPath path, byte[] data, CompressionType compression) throws IOException {
		synchronized (this.synchronizationLock) {
			assertArchiveIsOpen();

			final var hash = Hash.computeHash(data);
			final var uncompressedSize = data.length;
			switch (compression) {
				case LZMA:
					data = LzmaCodec.encode(data);
					break;
				case ZIP:
					data = ZipCodec.encode(data);
					break;
				default:
					break;
			}
			final var compressedSize = data.length;

			final var parentDirectory = this.indexFile.findOrCreateDirectory(path.getParent());
			final var optional = parentDirectory.getChild(path.getLastName());
			if (optional.isPresent()) {
				if (!optional.get().isFile()) {
					throw new IllegalArgumentException(); // TODO
				}
				final var file = optional.get().asFile();
				this.archiveFile.deleteData(file.getHash());

				file.overwrite(null, compression.flag, new Date().getTime(), (long) uncompressedSize, (long) compressedSize, hash, null);
				this.archiveFile.writeData(hash, data, false);
			} else {
				parentDirectory.newFile(path.getLastName(), compression.flag, new Date().getTime(), uncompressedSize, compressedSize, hash, 0);
				this.archiveFile.writeData(hash, data, false);
			}
		}
	}

	@Override
	public void validateArchive() throws BinaryIOException, IOException {
		synchronized (this.synchronizationLock) {
			assertArchiveIsOpen();
			this.indexFile.validateFile();
			this.archiveFile.validateFile();
		}
	}

	@Override
	public void delete(IdxPath path) throws IOException {
		synchronized (this.synchronizationLock) {
			assertArchiveIsOpen();

			final var optional = this.indexFile.find(path);
			if (optional.isEmpty()) {
				throw new IndexEntryNotFoundException(path.getFullName()); // TODO
			}

			final var file = optional.get().asFile();
			this.archiveFile.deleteData(file.getHash());
			file.getParent().delete(file.getName());
		}
	}

	@Override
	public void move(IdxPath from, IdxPath to) throws IOException {
		synchronized (this.synchronizationLock) {
			assertArchiveIsOpen();
			this.indexFile.move(from, to);
		}
	}

	@Override
	public Optional<IdxEntry> find(IdxPath path) {
		synchronized (this.synchronizationLock) {
			assertArchiveIsOpen();

			final var entry = this.indexFile.find(path);
			if (entry.isEmpty()) {
				return Optional.empty();
			}
			final var node = entry.get();
			if (node.isDirectory()) {
				return Optional.of(new IdxDirectoryImpl(this, node.asDirectory()));
			}
			return Optional.of(new IdxFileLinkImpl(this, node.asFile()));
		}
	}

	protected byte[] getData(IdxFileLink fileLink) {
		byte[] data;
		synchronized (this.synchronizationLock) {
			assertArchiveIsOpen();
			try {
				data = this.archiveFile.getData(fileLink.getHash());
			} catch (final Exception e) {
				throw new VaultException(e); // TODO
			}
		}

		switch (fileLink.getFlags()) {
			case 4 | 1:
				return LzmaCodec.decode(data, fileLink.getUncompressedSize());
			case 2 | 1:
				return ZipCodec.decode(data, fileLink.getUncompressedSize());
			default: // none
				if (data.length != fileLink.getUncompressedSize()) {
					throw new VaultException(); // TODO
				}
				return data;
		}
	}

	private void assertArchiveIsOpen() {
		if (isDisposed()) {
			throw new VaultDisposedException();
		}
	}

}
