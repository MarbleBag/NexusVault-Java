package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.archive.PackIndexOutOfBounds;
import nexusvault.archive.impl.ArchiveMemoryModel.MemoryBlock;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;

abstract class AbstractArchiveFile {

	protected final BufferedPackFile packFile;

	public AbstractArchiveFile() {
		packFile = new BufferedPackFile();
	}

	public void openFile(Path path) throws IOException {
		final boolean fileIsNew = !Files.exists(path);
		packFile.openFile(path);
		afterFileRead(fileIsNew);
	}

	public void closeFile() throws IOException {
		beforeFileClose();
		packFile.closeFile();
	}

	public Path getFile() {
		return packFile.getFile();
	}

	public boolean isFileOpen() {
		return packFile.isFileOpen();
	}

	protected void flushWrite() throws IOException {
		packFile.flushWrite();
	}

	protected StructPackHeader getPack(long index) throws PackIndexOutOfBounds {
		return packFile.getPack(index);
	}

	protected boolean isPackAvailable(long index) {
		return (0 <= index) && (index < packFile.getPackArraySize());
	}

	protected void checkPackAvailability(long packIdx) {
		if (!isPackAvailable(packIdx)) {
			final String error = String.format("Pack index %d invalid. Must be in range of [0,%d).", packIdx, getPackArraySize());
			throw new PackIndexOutOfBounds(error);
		}
	}

	protected int getPackArraySize() {
		return packFile.getPackArraySize();
	}

	protected BinaryReader getFileReader() throws IOException {
		return packFile.getFileReader();
	}

	protected BinaryWriter getFileWriter() throws IOException {
		return packFile.getFileWriter();
	}

	protected void overwritePack(StructPackHeader pack, long packIdx) throws IOException, PackIndexOutOfBounds {
		packFile.overwritePack(pack, packIdx);
	}

	protected long writeNewPack(StructPackHeader pack) throws IOException {
		return packFile.writeNewPack(pack);
	}

	protected MemoryBlock allocateMemory(long size) {
		return packFile.getMemoryModel().allocateMemory(size);
	}

	protected MemoryBlock findMemoryBlock(long offset) {
		return packFile.getMemoryModel().findBlockAt(offset);
	}

	protected void freeMemoryBlock(MemoryBlock block) {
		packFile.getMemoryModel().freeMemory(block);
	}

	protected StructPackHeader writeRootElement(StructRootBlock element) throws IOException {
		return packFile.writeRootElement(element);
	}

	public void enableWriteMode() throws IOException {
		packFile.enableWriteMode();
	}

	protected StructPackHeader getRootPack() throws PackIndexOutOfBounds {
		return getPack(getRootElement().headerIdx);
	}

	protected StructRootBlock getRootElement() {
		return packFile.getRootElement();
	}

	protected void setPackRootIdx(long index) {
		packFile.setPackRootIdx(index);
	}

	abstract protected void afterFileRead(boolean isFileNew) throws IOException;

	abstract protected void beforeFileClose() throws IOException;

}
