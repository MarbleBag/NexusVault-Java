package nexusvault.vault.index;

import java.util.List;
import java.util.Optional;

import nexusvault.vault.IdxPath;

public interface Node {

	public static interface DirectoryNode extends Node {
		DirectoryNode newDirectory(String name);

		FileNode newFile(String name, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk_034);

		void delete(String name);

		boolean hasChild(String name);

		Optional<Node> getChild(String name);

		List<Node> getChilds();

		List<DirectoryNode> getDirectories();

		List<FileNode> getFiles();

		Optional<Node> find(IdxPath path);

		Node findLast(IdxPath path);

		int countNodesInSubTree();
	}

	public static interface FileNode extends Node {
		int getFlags();

		long getWriteTime();

		long getUncompressedSize();

		long getCompressedSize();

		byte[] getHash();

		int getUnk_034();

		/**
		 * Sets only non-null values
		 */
		void overwrite(String name, Integer flags, Long writeTime, Long uncompressedSize, Long compressedSize, byte[] hash, Integer unk_034);

		void setFlags(int flags);

		void setWriteTime(long writeTime);

		void setUncompressedSize(long uncompressedSize);

		void setCompressedSize(long compressedSize);

		void setHash(byte[] hash);

		void setUnk_034(int unk_034);
	}

	String getName();

	void setName(String name);

	DirectoryNode getParent();

	boolean isDirectory();

	boolean isFile();

	FileNode asFile();

	DirectoryNode asDirectory();

	void moveTo(DirectoryNode parent);

	IdxPath toPath();
}
