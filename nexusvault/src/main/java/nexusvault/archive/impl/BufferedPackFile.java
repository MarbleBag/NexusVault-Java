package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.TreeMap;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;

final class BufferedPackFile implements PackFile {

	private final PackFile packFile;

	private final TreeMap<Integer, StructPackHeader> pendingPacks;
	private int minimalSize;

	public BufferedPackFile() {
		packFile = new BasePackFile();
		pendingPacks = new TreeMap<>();
	}

	@Override
	public void closeFile() throws IOException {
		flushWrite(false);
		packFile.closeFile();
		pendingPacks.clear();
	}

	public void setPackArrayMinimalCapacity(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("'size' must be greater than 0");
		}
		minimalSize = size;
	}

	@Override
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
				if (expectedPackIndex < packFile.getPackArraySize()) {
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

		if (flushSub) {
			packFile.flushWrite();
		}
	}

	@Override
	public void overwritePack(StructPackHeader pack, long packIdx) throws IOException {
		if (isPendingPack(packIdx)) {
			storePendingPack(pack, packIdx);
		} else {
			packFile.overwritePack(pack, packIdx);
		}
	}

	@Override
	public long writeNewPack(StructPackHeader pack) throws IOException {
		return storePendingPack(pack);
	}

	@Override
	public PackIdxSwap deletePack(long packIdx) throws IOException {
		if (isPendingPack(packIdx) && ((pendingPacks.size() - 1) == packIdx)) {
			pendingPacks.pollLastEntry();
			return null;
		} else if (pendingPacks.isEmpty()) {
			return packFile.deletePack(packIdx);
		}

		final Entry<Integer, StructPackHeader> lastEntry = pendingPacks.pollLastEntry();
		overwritePack(lastEntry.getValue(), packIdx);
		return new PackIdxSwap(lastEntry.getKey(), packIdx);
	}

	@Override
	public StructPackHeader getPack(long packIdx) {
		if (isPendingPack(packIdx)) {
			return getPendingPack(packIdx);
		} else {
			return packFile.getPack(packIdx);
		}
	}

	@Override
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

	@Override
	public void openFile(Path path) throws IOException {
		packFile.openFile(path);
	}

	@Override
	public boolean isFileOpen() {
		return packFile.isFileOpen();
	}

	@Override
	public Path getFile() {
		return packFile.getFile();
	}

	@Override
	public void enableWriteMode() throws IOException {
		packFile.enableWriteMode();
	}

	@Override
	public boolean isWriteModeEnabled() {
		return packFile.isWriteModeEnabled();
	}

	@Override
	public BinaryReader getFileReader() throws IOException {
		return packFile.getFileReader();
	}

	@Override
	public BinaryWriter getFileWriter() throws IOException {
		return packFile.getFileWriter();
	}

	@Override
	public ArchiveMemoryModel getMemoryModel() throws IllegalStateException {
		return packFile.getMemoryModel();
	}

	@Override
	public int getPackArrayCapacity() {
		return packFile.getPackArrayCapacity();
	}

	@Override
	public void setPackArrayAutoGrowSize(int value) {
		packFile.setPackArrayAutoGrowSize(value);
	}

	@Override
	public void setPackArrayCapacityTo(int minimalSize) throws IOException {
		packFile.setPackArrayCapacityTo(minimalSize);
	}

	@Override
	public boolean isPackArrayInitialized() {
		return packFile.isPackArrayInitialized();
	}

	@Override
	public StructRootBlock getRootElement() {
		return packFile.getRootElement();
	}

	@Override
	public StructPackHeader writeRootElement(StructRootBlock element) throws IOException {
		return packFile.writeRootElement(element);
	}

	@Override
	public void setPackRootIdx(long rootIdx) {
		packFile.setPackRootIdx(rootIdx);
	}

	@Override
	public long getPackRootIndex() {
		return packFile.getPackRootIndex();
	}

}
