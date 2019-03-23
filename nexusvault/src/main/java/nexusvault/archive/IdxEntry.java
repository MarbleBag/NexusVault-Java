package nexusvault.archive;

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
	 * @return
	 * @see IdxPath#getFullName()
	 */
	String getFullName();

	boolean isFile();

	boolean isDir();

	IdxFileLink asFile() throws IdxEntryNotAFileException;

	IdxDirectory asDirectory() throws IdxEntryNotADirectoryException;

	/**
	 * @return a path, starting at the root and ending with this entry
	 */
	IdxPath getPath();

	/**
	 * @return the {@link NexusArchive archive} this entry belongs to
	 */
	NexusArchive getArchive();

}