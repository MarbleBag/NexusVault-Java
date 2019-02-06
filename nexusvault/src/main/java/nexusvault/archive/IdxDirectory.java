package nexusvault.archive;

import java.util.List;

public interface IdxDirectory extends IdxEntry {

	List<IdxEntry> getChilds();

	int getChildCount();

	List<IdxEntry> getChildsDeep();

	int countSubTree();

	@Override
	String fullName();

	List<IdxDirectory> getSubDirectories();

	List<IdxFileLink> getFiles();

	IdxEntry getEntry(String path) throws IdxEntryNotFound;

	IdxFileLink getFile(String path) throws IdxEntryNotFound, IdxEntryNotAFile;

}