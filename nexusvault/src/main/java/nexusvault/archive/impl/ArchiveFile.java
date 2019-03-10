package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;

public interface ArchiveFile {

	public static ArchiveFile createArchiveFile() {
		return new BaseArchiveFile();
	}

	void openTarget(Path path) throws IOException;

	void closeTarget() throws IOException;

	Path getTarget();

	boolean isOpen();

	boolean hasArchiveData(byte[] hash);

	int getNumberOfEntries();

	BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException;

	void enableWriteMode() throws IOException;

	void setEstimatedNumberForWriteEntries(int count) throws IOException;

	void setArchiveData(byte[] hash, BinaryReader data) throws IOException;

	void flushWrite() throws IOException;

}
