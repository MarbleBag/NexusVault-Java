package nexusvault.archive;

public interface IdxEntry {

	String getName();

	IdxDirectory getParent();

	String fullName();

	boolean isFile();

	boolean isDir();

	IdxFileLink asFile();

	IdxDirectory asDirectory();

}