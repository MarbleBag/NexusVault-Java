package nexusvault.archive;

public interface IdxEntry {

	String getName();

	IdxDirectory getParent();

	String getFullName();

	boolean isFile();

	boolean isDir();

	IdxFileLink asFile() throws IdxEntryNotAFileException;

	IdxDirectory asDirectory() throws IdxEntryNotADirectoryException;

	IdxPath getPath();

	NexusArchiveReader getArchive();

}