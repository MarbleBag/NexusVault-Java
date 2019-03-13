package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;

interface PackFile {

	void openFile(Path path) throws IOException;

	void closeFile() throws IOException;

	boolean isFileOpen();

	Path getFile();

	void enableWriteMode() throws IOException;

	void flushWrite() throws IOException;

	boolean isWriteModeEnabled();

	BinaryReader getFileReader() throws IOException;

	BinaryWriter getFileWriter() throws IOException;

	/**
	 *
	 * @return
	 * @throws IllegalStateException
	 *             if this instance is not in write mode
	 * @see #enableWriteMode()
	 */
	ArchiveMemoryModel getMemoryModel() throws IllegalStateException;

	StructPackHeader getPack(long packIdx);

	int getPackArrayCapacity();

	int getPackArraySize();

	boolean isPackAvailable(long packIdx);

	boolean isPackWritable(long packIdx);

	void overwritePack(StructPackHeader pack, long packIdx) throws IOException;

	long writeNewPack(StructPackHeader pack) throws IOException;

	void setPackArrayAutoGrow(boolean value);

	void setPackArrayAutoGrowSize(int value);

	void initializePackArray() throws IOException;

	void setPackArrayCapacityTo(int minimalSize) throws IOException;

	boolean isPackArrayInitialized();

	StructRootBlock getRootElement();

	StructPackHeader writeRootElement(StructRootBlock element) throws IOException;

	void setPackRootIdx(long rootIdx);

	long getPackRootIndex();

}