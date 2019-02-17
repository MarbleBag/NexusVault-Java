package nexusvault.archive;

import java.nio.ByteBuffer;
import java.util.List;

public interface IdxDirectory extends IdxEntry {

	List<IdxEntry> getChilds();

	int getChildCount();

	List<IdxEntry> getChildsDeep();

	int countSubTree();

	List<IdxDirectory> getDirectories();

	List<IdxFileLink> getFiles();

	boolean hasEntry(String entryName);

	IdxEntry getEntry(String entryName) throws IdxEntryNotFound;

	IdxDirectory getDirectory(String directoryName) throws IdxEntryNotFound, IdxEntryNotADirectory;

	IdxFileLink getFileLink(String fileLinkName) throws IdxEntryNotFound, IdxEntryNotAFile;

	@Deprecated
	IdxDirectory createDirectory(String directoryName);

	@Deprecated
	IdxFileLink createFileLink(String fileLinkName, ByteBuffer data, int flags);

	@Deprecated
	void removeEntry(String entryName) throws IdxEntryNotFound;
}