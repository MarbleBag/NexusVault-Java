package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeMap;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;

import java.util.Map.Entry;

import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;

class BufferedPackFile implements PackFile {

	private PackFile packFile;

	private TreeMap<Integer, StructPackHeader> pendingPacks;
	private int minimalSize;

	public BufferedPackFile() {
		packFile = new BasePackFile();
		pendingPacks = new TreeMap<Integer, StructPackHeader>();
	}

	public void closeFile() throws IOException {
		flushWrite(false);
		packFile.closeFile();
		pendingPacks.clear();
	}

	public void setPackArrayMinimalCapacity(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("'size' must be greater than 0");
		}
		this.minimalSize = size;
	}
	
	public void flushWrite() throws IOException {
		flushWrite(true);
	}

	private void flushWrite(boolean flushSub) throws IOException {
		if (isWriteModeEnabled()) {
			final int size = Math.max(minimalSize, getPackArraySize());
			if (packFile.getPackArrayCapacity() < size) {
				packFile.setPackArrayCapacityTo(size);
			}

			for (final Entry<Integer, StructPackHeader> pendingPack : pendingPacks.entrySet()) {
				final long expectedPackIndex = pendingPack.getKey().longValue();
				if (packFile.isPackAvailable(expectedPackIndex)) {
					packFile.overwritePack(pendingPack.getValue(), expectedPackIndex);
				} else {
					final long actualPackIndex = packFile.writeNewPack(pendingPack.getValue());
					if (expectedPackIndex != actualPackIndex) {
						throw new IllegalStateException(); // TODO something did go badly wrong
					}
				}
			}
			pendingPacks.clear();
		}
		if(flushSub)
		packFile.flushWrite();
	}

	public void overwritePack(StructPackHeader pack, long packIdx) throws IOException {
		if (isPendingPack(packIdx)) {
			storePendingPack(pack, packIdx);
		} else {
			packFile.overwritePack(pack, packIdx);
		}
	}

	public long writeNewPack(StructPackHeader pack) throws IOException {
		return storePendingPack(pack);
	}

	public StructPackHeader getPack(long packIdx) {
		if (isPendingPack(packIdx)) {
			return getPendingPack(packIdx);
		} else {
			return packFile.getPack(packIdx);
		}
	}

	public int getPackArraySize() {
		return packFile.getPackArraySize() + pendingPacks.size();
	}

	private boolean isPendingPack(long packIdx) {
		return pendingPacks.containsKey(Integer.valueOf((int) packIdx));
	}

	private void storePendingPack(StructPackHeader pack, long packIdx) {
		if (!isPendingPack(packIdx) && (packIdx != pendingPacks.size())) {
			throw new IndexOutOfBoundsException(String.format("Pack index out of bounds [%d,%d)", packFile.getPackArraySize(), getPackArraySize()));
		}
		pendingPacks.put(Integer.valueOf((int) packIdx), pack);
	}

	private long storePendingPack(StructPackHeader pack) {
		final long packIdx = getPackArraySize();
		pendingPacks.put(Integer.valueOf((int) packIdx), pack);
		return packIdx;
	}

	private StructPackHeader getPendingPack(long packIdx) {
		return pendingPacks.get(Integer.valueOf((int) packIdx));
	}

	public void openFile(Path path) throws IOException {
		packFile.openFile(path);
	}

	public boolean isFileOpen() {
		return packFile.isFileOpen();
	}

	public Path getFile() {
		return packFile.getFile();
	}

	public void enableWriteMode() throws IOException {
		packFile.enableWriteMode();
	}

	public boolean isWriteModeEnabled() {
		return packFile.isWriteModeEnabled();
	}

	public BinaryReader getFileReader() throws IOException {
		return packFile.getFileReader();
	}

	public BinaryWriter getFileWriter() throws IOException {
		return packFile.getFileWriter();
	}

	public ArchiveMemoryModel getMemoryModel() throws IllegalStateException {
		return packFile.getMemoryModel();
	}

	public int getPackArrayCapacity() {
		return packFile.getPackArrayCapacity();
	}

	public boolean isPackAvailable(long packIdx) {
		return packFile.isPackAvailable(packIdx) || isPendingPack(packIdx);
	}

	public boolean isPackWritable(long packIdx) {
		return packFile.isPackWritable(packIdx);
	}

	public void setPackArrayAutoGrow(boolean value) {
		packFile.setPackArrayAutoGrow(value);
	}

	public void setPackArrayAutoGrowSize(int value) {
		packFile.setPackArrayAutoGrowSize(value);
	}

	public void initializePackArray() throws IOException {
		packFile.initializePackArray();
	}

	public void setPackArrayCapacityTo(int minimalSize) throws IOException {
		packFile.setPackArrayCapacityTo(minimalSize);
	}

	public boolean isPackArrayInitialized() {
		return packFile.isPackArrayInitialized();
	}

	public StructRootBlock getRootElement() {
		return packFile.getRootElement();
	}

	public StructPackHeader writeRootElement(StructRootBlock element) throws IOException {
		return packFile.writeRootElement(element);
	}

	public void setPackRootIdx(long rootIdx) {
		packFile.setPackRootIdx(rootIdx);
	}

	public long getPackRootIndex() {
		return packFile.getPackRootIndex();
	}

}
