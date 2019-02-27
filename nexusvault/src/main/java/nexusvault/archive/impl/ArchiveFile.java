package nexusvault.archive.impl;

import java.io.IOException;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.struct.StructArchiveEntry;
import nexusvault.archive.struct.StructPackHeader;

public interface ArchiveFile {

	int getPackCount();

	int getPackRootIdx();

	StructArchiveEntry getEntry(byte[] hash) throws ArchiveEntryNotFoundException;

	StructPackHeader getPack(int packIdx);

	BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException;

	boolean isDisposed();

	void dispose();
}