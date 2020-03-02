package nexusvault.archive;

import java.util.List;

public interface IdxDirectory extends IdxEntry {

	List<IdxEntry> getChilds();

	int getChildCount();

	List<IdxEntry> getChildsDeep();

	int countSubTree();

	List<IdxDirectory> getDirectories();

	List<IdxFileLink> getFiles();

	boolean hasEntry(String entryName);

	IdxEntry getEntry(String entryName) throws IdxEntryNotFoundException;

	IdxDirectory getDirectory(String directoryName) throws IdxEntryNotFoundException, IdxEntryNotADirectoryException;

	IdxFileLink getFileLink(String fileLinkName) throws IdxEntryNotFoundException, IdxEntryNotAFileException;

}