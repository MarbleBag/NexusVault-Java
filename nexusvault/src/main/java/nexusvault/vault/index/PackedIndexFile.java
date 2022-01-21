package nexusvault.vault.index;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import kreed.io.util.BinaryIOException;
import kreed.io.util.BinaryWriter;
import nexusvault.shared.exception.NexusVaultException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;
import nexusvault.vault.IdxPath;
import nexusvault.vault.index.IndexException.IndexEntryNotADirectoryException;
import nexusvault.vault.index.IndexException.IndexEntryNotFoundException;
import nexusvault.vault.index.Node.DirectoryNode;
import nexusvault.vault.index.Node.FileNode;
import nexusvault.vault.index.NodeImpl.DirectoryNodeImpl;
import nexusvault.vault.index.NodeImpl.FileNodeImpl;
import nexusvault.vault.pack.PackException.PackIndexInvalidException;
import nexusvault.vault.pack.PackedFile;
import nexusvault.vault.struct.StructIndexDirectory;
import nexusvault.vault.struct.StructIndexFile;
import nexusvault.vault.struct.StructIndexRootElement;

public final class PackedIndexFile implements Closeable {

	private static final int SIGNATURE = StructIndexRootElement.SIGNATURE_AIDX;
	private static final int VERSION = 1;

	private StructIndexRootElement rootElement;
	private DirectoryNodeImpl root;

	private boolean dirty;

	private final PackedFile file = new PackedFile();

	private final Set<Integer> deletedDirectories = new HashSet<>();

	public PackedIndexFile(Path path) throws IOException {
		open(path);
	}

	public PackedIndexFile() {
	}

	public DirectoryNode getRoot() {
		return this.root;
	}

	public void open(Path path) throws IOException {
		this.file.open(path);

		if (this.file.getRootIndex() > 0) {
			try (var reader = this.file.readEntry(this.file.getRootIndex())) {
				this.rootElement = new StructIndexRootElement(reader);

				if (this.rootElement.version != VERSION) {
					throw new VersionMismatchException("Packed index file", VERSION, this.rootElement.version);
				}

				if (this.rootElement.signature != SIGNATURE) {
					throw new SignatureMismatchException("Packed index file", SIGNATURE, this.rootElement.signature);
				}
			}
		} else {
			this.rootElement = new StructIndexRootElement(SIGNATURE, VERSION, 0, 0);
			this.file.setRootIndex(this.file.newEntry(StructIndexRootElement.SIZE_IN_BYTES));
			this.dirty = true;
		}

		this.root = new DirectoryNodeImpl(null, "", this.rootElement.headerIdx) {
			@Override
			public PackedIndexFile getIndexFile() {
				return PackedIndexFile.this;
			}

			@Override
			public void setName(String value) {

			}

			@Override
			protected void setNeedUpdateFlag() {
				super.setNeedUpdateFlag();
				PackedIndexFile.this.dirty = true;
			}
		};
	}

	public boolean isOpen() {
		return this.file.isOpen();
	}

	public void validateFile() throws BinaryIOException, IOException {
		this.file.validateFile();
	}

	public int getBuildNumber() {
		return this.rootElement.buildNumber;
	}

	public void setBuildNumber(int buildNumber) {
		this.dirty |= this.rootElement.buildNumber != buildNumber;
		this.rootElement.buildNumber = buildNumber;
	}

	@Override
	public void close() throws IOException {
		if (this.dirty) {
			writeToFile();
		}
		this.file.close();
	}

	public Optional<Node> find(IdxPath path) {
		return getRoot().find(path);
	}

	public Node findLast(IdxPath path) {
		return getRoot().findLast(path);
	}

	public DirectoryNode findOrCreateDirectory(IdxPath path) throws IOException {
		if (path.isRoot()) {
			return getRoot();
		}

		DirectoryNode node = getRoot();
		final var iterator = path.iterator();

		for (; iterator.hasNext();) {
			final var name = iterator.next();
			final var optional = node.getChild(name);
			if (optional.isPresent()) {
				final var nextNode = optional.get();
				if (nextNode.isFile()) {
					throw new IndexEntryNotADirectoryException(nextNode.toPath().getFullName());
				}
				node = nextNode.asDirectory();
			} else {
				node = node.newDirectory(name);
				break;
			}
		}

		for (; iterator.hasNext();) {
			node = node.newDirectory(iterator.next());
		}

		return node;
	}

	public DirectoryNode newDirectory(IdxPath path) throws IOException {
		if (path.isRoot()) {
			throw new IllegalArgumentException("empty path");
		}

		return findOrCreateDirectory(path);
	}

	public FileNode newFile(IdxPath path, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk_034) throws IOException {
		if (path.isRoot()) {
			throw new IllegalArgumentException("empty path");
		}

		final var parent = findOrCreateDirectory(path.getParent());
		return parent.newFile(path.getLastName(), flags, writeTime, uncompressedSize, compressedSize, hash, unk_034);
	}

	public void delete(IdxPath path) throws IOException {
		if (path.isRoot()) {
			throw new IllegalArgumentException("empty path");
		}

		final var optional = find(path.getParent());
		if (optional.isEmpty()) {
			throw new IndexEntryNotFoundException(path.getFullName());
		}

		final var node = optional.get().asDirectory();
		node.delete(path.getLastName());
	}

	public void move(IdxPath from, IdxPath to) throws IOException {
		if (from.isRoot()) {
			throw new IllegalArgumentException("empty path");
		}

		final var optionalFrom = find(from);
		if (optionalFrom.isEmpty()) {
			throw new IndexEntryNotFoundException(from.getFullName());
		}

		final var newParent = findOrCreateDirectory(to);
		optionalFrom.get().moveTo(newParent);
	}

	protected List<NodeImpl> loadChilds(DirectoryNodeImpl directoryNode) {
		StructIndexDirectory[] directoryData;
		StructIndexFile[] fileData;
		String nameTwine;

		try (var reader = this.file.readEntry(directoryNode.directoryIndex)) {
			directoryData = new StructIndexDirectory[(int) reader.readUInt32()];
			fileData = new StructIndexFile[(int) reader.readUInt32()];
			for (var i = 0; i < directoryData.length; ++i) {
				directoryData[i] = new StructIndexDirectory(reader);
			}
			for (var i = 0; i < fileData.length; ++i) {
				fileData[i] = new StructIndexFile(reader);
			}

			final var nameBytes = new byte[(int) (reader.size() - reader.position())];
			reader.readInt8(nameBytes, 0, nameBytes.length);
			nameTwine = new String(nameBytes, StandardCharsets.UTF_8);
		} catch (final IOException e) {
			throw new NexusVaultException(e); // TODO
		}

		final var entries = new ArrayList<NodeImpl>(directoryData.length + fileData.length);

		for (final StructIndexDirectory element : directoryData) {
			final var nameOffset = element.nameOffset;
			final var nullTerminator = nameTwine.indexOf(0, nameOffset);
			final var name = nameTwine.substring(nameOffset, nullTerminator);
			entries.add(new DirectoryNodeImpl(directoryNode, name, element.directoryIndex));
		}

		for (final StructIndexFile element : fileData) {
			final var nameOffset = element.nameOffset;
			final var nullTerminator = nameTwine.indexOf(0, nameOffset);
			final var name = nameTwine.substring(nameOffset, nullTerminator);
			entries.add(new FileNodeImpl(directoryNode, name, element.flags, element.writeTime, element.uncompressedSize, element.compressedSize, element.hash,
					element.unk_034));
		}

		return entries;
	}

	protected void markDirectoryForDeletion(int directoryIndex) {
		if (directoryIndex <= 0) {
			return;
		}

		this.deletedDirectories.add(directoryIndex);
		this.dirty = true;
	}

	private static final class UpdateDirectory {
		private final long size;
		private final DirectoryNodeImpl directory;

		public UpdateDirectory(long size, DirectoryNodeImpl directory) {
			this.size = size;
			this.directory = directory;
		}
	}

	public void writeToFile() throws IOException {
		{
			final var indices = new LinkedList<>(this.deletedDirectories);
			this.deletedDirectories.clear();

			while (!indices.isEmpty()) {
				final var directory = indices.pop();
				try (var reader = this.file.readEntry(directory)) {
					final var childCount = (int) reader.readUInt32();
					reader.readInt32(); // skip
					for (var i = 0; i < childCount; ++i) {
						final var child = new StructIndexDirectory(reader);
						indices.push(child.directoryIndex);
					}
				}
				this.file.deleteEntry(directory);
			}
		}

		final var updateNodes = new LinkedList<UpdateDirectory>();
		{
			final var visitNode = new LinkedList<DirectoryNodeImpl>();
			visitNode.add((DirectoryNodeImpl) getRoot());

			while (!visitNode.isEmpty()) {
				final var directory = visitNode.pop();
				if (directory.hasNodeFlag(NodeImpl.NODE_FLAG_UPDATE)) {
					final var neededCapacity = computeDirectorySize(directory);
					updateNodes.push(new UpdateDirectory(neededCapacity, directory));
					if (directory.directoryIndex != 0) {
						final var availableCapacity = this.file.entryCapacity(directory.directoryIndex);
						if (availableCapacity < neededCapacity) {
							this.file.releaseEntry(directory.directoryIndex);
							directory.directoryIndex = 0;
						}
					}
				}
				if (directory.hasNodeFlag(NodeImpl.NODE_FLAG_CHILD_UPDATE)) {
					final var subDirectories = directory.getDirectories();
					for (final var subDir : subDirectories) {
						visitNode.push((DirectoryNodeImpl) subDir);
					}
				}
				directory.clearNodeFlags();
			}

			updateNodes.sort((a, b) -> Long.compare(a.size, b.size));
		}

		for (final var updateNode : updateNodes) {
			if (updateNode.directory.directoryIndex <= 0) {
				updateNode.directory.directoryIndex = (int) this.file.newEntry(updateNode.size);
			}
			writeDirectory(updateNode.directory);
		}

		this.rootElement.headerIdx = ((DirectoryNodeImpl) getRoot()).directoryIndex;
		try (var writer = this.file.writeEntry(this.file.getRootIndex())) {
			this.rootElement.write(writer);
		}

		this.dirty = false;
	}

	private int computeDirectorySize(DirectoryNodeImpl directory) {
		final var directories = directory.getDirectories();
		final var files = directory.getFiles();

		var entrySizeInBytes = 2 * Integer.BYTES; // number of directories & files
		// number of structs
		entrySizeInBytes += directories.size() * StructIndexDirectory.SIZE_IN_BYTES;
		entrySizeInBytes += files.size() * StructIndexFile.SIZE_IN_BYTES;

		// space for names
		for (final var dir : directories) {
			entrySizeInBytes += dir.getName().length() + 1; // 8 byte per character + null termination
		}

		for (final var file : files) {
			entrySizeInBytes += file.getName().length() + 1; // 8 byte per character + null termination
		}

		return entrySizeInBytes;
	}

	@SuppressWarnings("unchecked")
	private void writeDirectory(DirectoryNodeImpl directory) throws IOException {
		if (directory.directoryIndex <= 0) {
			throw new PackIndexInvalidException(directory.directoryIndex);
		}

		final var directories = (List<DirectoryNodeImpl>) (List<?>) directory.getDirectories();
		final var files = (List<FileNodeImpl>) (List<?>) directory.getFiles();
		var nameOffset = 0;
		try (var writer = this.file.writeEntry(directory.directoryIndex)) {
			writer.writeInt32(directories.size());
			writer.writeInt32(files.size());
			for (final var dir : directories) {
				writer.writeInt32(nameOffset);
				writer.writeInt32(dir.directoryIndex);
				nameOffset += dir.name.length() + 1;
			}
			for (final var file : files) {
				writer.writeInt32(nameOffset);
				writer.writeInt32(file.flags);
				writer.writeInt64(file.writeTime);
				writer.writeInt64(file.uncompressedSize);
				writer.writeInt64(file.compressedSize);
				writer.writeInt8(file.hash, 0, 20);
				writer.writeInt32(file.unk_034);
				nameOffset += file.name.length() + 1;
			}
			for (final var dir : directories) {
				writeName(writer, dir.name);
			}
			for (final var file : files) {
				writeName(writer, file.name);
			}
		}
	}

	private void writeName(BinaryWriter writer, String name) {
		final byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
		if (name.length() != nameBytes.length) {
			throw new IllegalArgumentException(
					String.format("Characters which span multiple codepoints are not supported in file or directory names. Invalid Name: %s", name));
		}
		writer.writeInt8(nameBytes, 0, nameBytes.length);
		writer.writeInt8(0); // 0 terminated
	}

}
