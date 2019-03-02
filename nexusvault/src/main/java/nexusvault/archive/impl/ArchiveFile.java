package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;

public interface ArchiveFile {

	public static ArchiveFile createArchiveFile() {
		return new BaseArchiveFile();
	}

	void setTarget(Path path) throws IOException;

	void dispose();

	boolean isDisposed();

	boolean hasArchiveData(byte[] hash);

	int getNumberOfEntries();

	BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException;

	void prepareWriteMode() throws IOException;

	void setNumberOfExpectedEntries(int count);

	void setArchiveData(byte[] hash, BinaryReader data) throws IOException;

}
