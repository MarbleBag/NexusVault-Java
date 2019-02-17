package nexusvault.archive;

public interface IdxEntry {

	String getName();

	IdxDirectory getParent();

	String getFullName();

	boolean isFile();

	boolean isDir();

	IdxFileLink asFile();

	IdxDirectory asDirectory();

	IdxPath getPath();

	NexusArchive getArchive();

}