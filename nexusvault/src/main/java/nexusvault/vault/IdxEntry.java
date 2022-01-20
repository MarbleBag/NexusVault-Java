package nexusvault.vault;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import nexusvault.vault.index.IndexException.IndexEntryNotADirectoryException;
import nexusvault.vault.index.IndexException.IndexEntryNotAFileException;
import nexusvault.vault.index.IndexException.IndexEntryNotFoundException;

/**
 * Represents an entry in a {@link NexusArchive}
 */
public interface IdxEntry {

	/**
	 * @return the name of this entry
	 */
	String getName();

	/**
	 * @return the parent of this entry or null, if this entry is the root
	 */
	IdxDirectory getParent();

	/**
	 * The full name of an entry is equal to the {@link IdxPath#getFullName() full name} of a {@link #getPath() path} that is starting at the root and ending
	 * with this entry.
	 *
	 * @return the full name of this entry
	 * @see IdxPath#getFullName()
	 */
	String getFullName();

	boolean isFile();

	boolean isDirectory();

	IdxFileLink asFile() throws IndexEntryNotAFileException;

	IdxDirectory asDirectory() throws IndexEntryNotADirectoryException;

	/**
	 * @return a path, starting at the root and ending with this entry
	 */
	IdxPath getPath();

	/**
	 * @return the {@link NexusArchive archive} this entry belongs to
	 */
	NexusArchive getArchive();

	/**
	 * A {@link IdxEntry} which represents a link to a readable resource in an archive. This class provides functionality to access its linked resource as well
	 * as its meta-data.
	 */
	public static interface IdxFileLink extends IdxEntry {

		byte[] getHash();

		int getFlags();

		long getUncompressedSize();

		long getCompressedSize();

		long getWriteTime();

		String getFileEnding();

		String getNameWithoutFileExtension();

		/**
		 * Reads the linked resource in a thread safe manner into a {@link ByteBuffer}
		 *
		 * @return A {@linkplain ByteBuffer} containing all data this {@link IdxFileLink} represents
		 * @throws IOException
		 */
		byte[] getData() throws IOException;

	}

	public static interface IdxDirectory extends IdxEntry {

		List<IdxEntry> getEntries();

		List<IdxDirectory> getDirectories();

		List<IdxFileLink> getFiles();

		boolean hasEntry(String entryName);

		Optional<IdxEntry> getEntry(String entryName);

		IdxDirectory getDirectory(String directoryName) throws IndexEntryNotFoundException, IndexEntryNotADirectoryException;

		IdxFileLink getFileLink(String fileLinkName) throws IndexEntryNotFoundException, IndexEntryNotAFileException;

		Optional<IdxEntry> find(IdxPath path);

		int countNodesInSubTree();

	}

}
