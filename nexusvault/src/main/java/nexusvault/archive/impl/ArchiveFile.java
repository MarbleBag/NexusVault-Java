package nexusvault.archive.impl;

import java.io.IOException;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;

public interface ArchiveFile {

	int getPackCount();

	int getPackRootIdx();

	BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException;

	boolean isDisposed();

	void dispose();
}