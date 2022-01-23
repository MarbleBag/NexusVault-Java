package nexusvault.vault.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import nexusvault.vault.IdxPath;
import nexusvault.vault.index.IndexException.IndexEntryNotADirectoryException;
import nexusvault.vault.index.IndexException.IndexEntryNotAFileException;
import nexusvault.vault.index.IndexException.IndexNameCollisionException;

abstract class NodeImpl implements Node {

	static class DirectoryNodeImpl extends NodeImpl implements DirectoryNode {

		private final List<NodeImpl> childs = new ArrayList<>();
		private boolean childsLoaded = false;
		protected int directoryIndex = 0;

		protected DirectoryNodeImpl(DirectoryNodeImpl parent, String name, int directoryIndex) {
			super(parent, name);
			this.directoryIndex = directoryIndex;
			this.childsLoaded = directoryIndex <= 0;
			if (directoryIndex <= 0) {
				setNeedUpdateFlag();
			}
		}

		private List<NodeImpl> internalChilds() throws IOException {
			if (!this.childsLoaded) {
				this.childsLoaded = true;
				final var childs = getIndexFile().loadChilds(this);
				this.childs.addAll(childs);
			}
			return this.childs;
		}

		@Override
		public DirectoryNodeImpl newDirectory(String name) throws IOException {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("'name' must not be null or blank");
			}

			if (hasChild(name)) {
				throw new IndexNameCollisionException(toPath().resolve(name).getFullName());
			}

			final var child = new DirectoryNodeImpl(this, name, 0);
			internalChilds().add(child);
			setNeedUpdateFlag();
			return child;
		}

		@Override
		public FileNodeImpl newFile(String name, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk_034)
				throws IOException {
			if (name == null || name.isBlank()) {
				throw new IllegalArgumentException("'name' must not be null or blank");
			}

			if (hasChild(name)) {
				throw new IndexNameCollisionException(toPath().resolve(name).getFullName());
			}

			final var child = new FileNodeImpl(this, name, flags, writeTime, uncompressedSize, compressedSize, hash, unk_034);
			internalChilds().add(child);
			setNeedUpdateFlag();
			return child;
		}

		@Override
		public boolean hasChild(String name) throws IOException {
			return getChild(name).isPresent();
		}

		@Override
		public void delete(String name) throws IOException {
			for (final var it = internalChilds().iterator(); it.hasNext();) {
				final var child = it.next();
				if (child.name.equalsIgnoreCase(name)) {
					it.remove();
					child.parent = null;

					setNeedUpdateFlag();

					if (child.isDirectory()) {
						final var childIndex = ((DirectoryNodeImpl) child).directoryIndex;
						if (childIndex > 0) {
							final var indexFile = getIndexFile();
							if (indexFile != null) {
								indexFile.markDirectoryForDeletion(childIndex);
							}
						}
					}

					break;
				}
			}
		}

		private NodeImpl child(String name) throws IOException {
			for (final var child : internalChilds()) {
				if (child.name.equalsIgnoreCase(name)) {
					return child;
				}
			}
			return null;
		}

		@Override
		public Optional<Node> getChild(String name) throws IOException {
			return Optional.ofNullable(child(name));
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Node> getChilds() throws IOException {
			return Collections.unmodifiableList((List<Node>) (List<?>) internalChilds());
		}

		@Override
		public final List<DirectoryNode> getDirectories() throws IOException {
			return internalChilds().stream().filter(NodeImpl::isDirectory).map(NodeImpl::asDirectory).collect(Collectors.toList());
		}

		@Override
		public final List<FileNode> getFiles() throws IOException {
			return internalChilds().stream().filter(NodeImpl::isFile).map(NodeImpl::asFile).collect(Collectors.toList());
		}

		protected void setNeedUpdateFlag() {
			setNodeFlag(NODE_FLAG_UPDATE);
			notifyParentAboutChildUpdate();
		}

		private void notifyParentAboutChildUpdate() {
			final var parent = getParent();
			if (parent != null && !parent.hasNodeAnyFlag(NODE_FLAG_CHILD_UPDATE)) {
				parent.setNodeFlag(NODE_FLAG_CHILD_UPDATE);
				parent.notifyParentAboutChildUpdate();
			}
		}

		@Override
		public boolean isDirectory() {
			return true;
		}

		@Override
		public boolean isFile() {
			return false;
		}

		@Override
		public Optional<Node> find(IdxPath path) throws IOException {
			Objects.requireNonNull(path, "path");

			if (path.isRoot()) {
				return Optional.of(this);
			}

			Node node = this;
			for (final String string : path) {
				if (node.isFile()) {
					return Optional.empty();
				}
				node = ((DirectoryNodeImpl) node).child(string);
				if (node == null) {
					return Optional.empty();
				}
			}
			return Optional.of(node);
		}

		@Override
		public NodeImpl findLast(IdxPath path) throws IOException {
			Objects.requireNonNull(path, "path");

			if (path.isRoot()) {
				return this;
			}

			NodeImpl node = this;
			for (final String string : path) {
				if (node.isFile()) {
					return node;
				}

				final var nextNode = ((DirectoryNodeImpl) node).child(string);
				if (nextNode == null) {
					return node;
				}

				node = nextNode;
			}
			return node;
		}

		@Override
		public int countNodesInSubTree() throws IOException {
			final var fringe = new LinkedList<DirectoryNodeImpl>();
			fringe.add(this);
			int result = 1;
			while (!fringe.isEmpty()) {
				final var dir = fringe.pollFirst();
				final var childs = dir.internalChilds();
				result += childs.size();
				for (final var child : childs) {
					if (child.isDirectory()) {
						fringe.addLast(child.asDirectory());
					}
				}
			}
			return result;
		}

	}

	static class FileNodeImpl extends NodeImpl implements FileNode {
		protected int flags;
		protected long writeTime;
		protected long uncompressedSize;
		protected long compressedSize;
		protected byte[] hash;
		protected int unk_034;

		protected FileNodeImpl(DirectoryNodeImpl parent, String name, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash,
				int unk_034) {

			super(parent, name);

			Objects.requireNonNull(hash, "hash");
			if (hash.length != 20) {
				throw new IllegalArgumentException("'hash' must be of length 20");
			}

			this.flags = flags;
			this.writeTime = writeTime;
			this.uncompressedSize = uncompressedSize;
			this.compressedSize = compressedSize;
			this.hash = hash;
			this.unk_034 = unk_034;
		}

		@Override
		public int getFlags() {
			return this.flags;
		}

		@Override
		public long getWriteTime() {
			return this.writeTime;
		}

		@Override
		public long getUncompressedSize() {
			return this.uncompressedSize;
		}

		@Override
		public long getCompressedSize() {
			return this.compressedSize;
		}

		@Override
		public byte[] getHash() {
			return this.hash;
		}

		@Override
		public int getUnk_034() {
			return this.unk_034;
		}

		@Override
		public void overwrite(String name, Integer flags, Long writeTime, Long uncompressedSize, Long compressedSize, byte[] hash, Integer unk_034) {
			if (name != null) {
				setName(name);
			}

			var change = false;

			if (flags != null) {
				change |= flags.intValue() != this.flags;
				this.flags = flags;
			}
			if (writeTime != null) {
				change |= writeTime.longValue() != this.writeTime;
				this.writeTime = writeTime;
			}
			if (uncompressedSize != null) {
				change |= uncompressedSize.longValue() != this.uncompressedSize;
				this.uncompressedSize = uncompressedSize;
			}
			if (compressedSize != null) {
				change |= compressedSize.longValue() != this.compressedSize;
				this.compressedSize = compressedSize;
			}
			if (hash != null) {
				if (hash.length != 20) {
					throw new IllegalArgumentException("'hash' must be of length 20");
				}
				change |= !Arrays.equals(this.hash, hash);
				this.hash = hash;
			}
			if (unk_034 != null) {
				change |= flags.intValue() != this.flags;
				this.unk_034 = unk_034;
			}

			if (change) {
				informParentAboutUpdate();
			}
		}

		@Override
		public void setFlags(int flags) {
			this.flags = flags;
			informParentAboutUpdate();
		}

		@Override
		public void setWriteTime(long writeTime) {
			this.writeTime = writeTime;
			informParentAboutUpdate();
		}

		@Override
		public void setUncompressedSize(long uncompressedSize) {
			this.uncompressedSize = uncompressedSize;
			informParentAboutUpdate();
		}

		@Override
		public void setCompressedSize(long compressedSize) {
			this.compressedSize = compressedSize;
			informParentAboutUpdate();
		}

		@Override
		public void setHash(byte[] hash) {
			Objects.requireNonNull(hash, "hash");
			if (hash.length != 20) {
				throw new IllegalArgumentException("'hash' must be of length 20");
			}
			this.hash = hash;
			informParentAboutUpdate();
		}

		@Override
		public void setUnk_034(int unk_034) {
			this.unk_034 = unk_034;
			informParentAboutUpdate();
		}

		@Override
		public boolean isDirectory() {
			return false;
		}

		@Override
		public boolean isFile() {
			return true;
		}

	}

	/** Indicates that this node is new and, depending on its type, needs a data field and its parent needs to update its data */
	// protected static final int NODE_FLAG_NEW = 1 << 0;
	/** Indicates that the name of this node changed and its parent needs to update its data */
	protected static final int NODE_FLAG_NAME_CHANGED = 1 << 2;
	/** Indicates that the data of this node needs to be updated */
	protected static final int NODE_FLAG_UPDATE = 1 << 0;
	/** Indicates that the data of a child of this node needs to be updated */
	protected static final int NODE_FLAG_CHILD_UPDATE = 1 << 2;

	protected String name;
	private DirectoryNodeImpl parent;
	protected int nodeFlags;

	private NodeImpl(DirectoryNodeImpl parent, String name) {
		Objects.requireNonNull(name, "name");

		this.parent = parent;
		this.name = name;
	}

	final protected boolean hasNodeFlag(int flags) {
		return (this.nodeFlags & flags) == flags;
	}

	final protected boolean hasNodeAnyFlag(int flags) {
		return (this.nodeFlags & flags) != 0;
	}

	final protected void setNodeFlag(int flags) {
		this.nodeFlags |= flags;
	}

	protected void clearNodeFlags() {
		this.nodeFlags = 0;
	}

	public PackedIndexFile getIndexFile() {
		return this.parent.getIndexFile();
	}

	@Override
	public final String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		Objects.requireNonNull(name, "name");

		if (name.equals(this.name)) {
			return;
		}

		this.name = name;
		informParentAboutUpdate();
	}

	protected void informParentAboutUpdate() {
		if (this.parent != null) {
			this.parent.setNeedUpdateFlag();
		}
	}

	@Override
	final public DirectoryNodeImpl getParent() {
		return this.parent;
	}

	@Override
	final public void moveTo(DirectoryNode parent) throws IOException {
		Objects.requireNonNull(parent, "parent");

		final var newParent = (DirectoryNodeImpl) parent;

		if (this.parent == newParent) {
			return;
		}

		if (newParent.getIndexFile() != getIndexFile()) {
			throw new IllegalArgumentException("unable to move between index files");
		}

		if (newParent.hasChild(getName())) {
			throw new IndexNameCollisionException(newParent.toPath().resolve(getName()).getFullName());
		}

		final var oldParent = this.parent;
		oldParent.internalChilds().remove(this);
		oldParent.setNeedUpdateFlag();

		this.parent = newParent;
		newParent.internalChilds().add(this);
		newParent.setNeedUpdateFlag();
	}

	@Override
	final public IdxPath toPath() {
		if (this.parent == null) {
			return IdxPath.createPath(getName());
		}
		return getParent().toPath().resolve(getName());
	}

	@Override
	final public FileNodeImpl asFile() {
		if (!isFile()) {
			throw new IndexEntryNotAFileException(toPath().getFullName());
		}
		return (FileNodeImpl) this;
	}

	@Override
	final public DirectoryNodeImpl asDirectory() {
		if (!isDirectory()) {
			throw new IndexEntryNotADirectoryException(toPath().getFullName());
		}
		return (DirectoryNodeImpl) this;
	}
}
