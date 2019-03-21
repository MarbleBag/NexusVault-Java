package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.ArchiveHashCollisionException;
import nexusvault.archive.impl.ArchiveMemoryModel.MemoryBlock;
import nexusvault.archive.impl.PackFile.PackIdxSwap;
import nexusvault.archive.struct.StructAARC;
import nexusvault.archive.struct.StructArchiveEntry;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;

final class BaseArchiveFile extends AbstractArchiveFile implements ArchiveFile {

	private Map<String, Integer> entryLookUp;
	private final Map<String, Integer> queuedEntryLookUp;
	private ArrayList<StructArchiveEntry> entries;

	private int archiveArrayMinimalSize;
	private int archiveArrayCapacity;

	public BaseArchiveFile() {
		super();
		entryLookUp = new HashMap<>();
		queuedEntryLookUp = new HashMap<>();
		entries = new ArrayList<>();
	}

	@Override
	protected void afterFileRead(boolean isFileNew) throws IOException {
		archiveArrayCapacity = 0;

		if (isFileNew) {
			enableWriteMode();
			packFile.setPackArrayAutoGrowSize(1000);

			final long emptyPack = writeNewPack(new StructPackHeader());
			final long archiveEntryPack = writeNewPack(new StructPackHeader());

			final StructRootBlock aarc = new StructRootBlock(StructRootBlock.SIGNATURE_AARC, 1, 0, (int) archiveEntryPack);
			final StructPackHeader pack = writeRootElement(aarc);
			final long aidxIndex = writeNewPack(pack);
			setPackRootIdx(aidxIndex);

			computeArchiveArrayCapacity();
		} else {
			final StructRootBlock aarc = getRootElement();
			if (aarc == null) {
				throw new IllegalStateException(); // TODO
			}
			if (aarc.signature != StructAARC.SIGNATURE_AARC) {
				throw new SignatureMismatchException("Archive file", StructAARC.SIGNATURE_AARC, aarc.signature);
			}

			loadArchivEntries();
		}

	}

	@Override
	public BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException {
		final StructArchiveEntry entry = getArchiveEntry(hash);
		final StructPackHeader pack = getPack(entry);

		if ((pack.size > Integer.MAX_VALUE) || (pack.size < 0)) {
			throw new IntegerOverflowException("Archive file: data exceeds " + Integer.MAX_VALUE + " bytes");
		}

		try (BinaryReader reader = getFileReader()) {
			reader.seek(Seek.BEGIN, pack.getOffset());
			final ByteBuffer data = ByteBuffer.allocate((int) pack.size).order(reader.getOrder());
			reader.readTo(data);
			data.flip();
			return new ByteBufferBinaryReader(data);
		}
	}

	private StructArchiveEntry getArchiveEntry(byte[] hash) throws ArchiveEntryNotFoundException {
		if (hash == null) {
			throw new IllegalArgumentException("'entry' must not be null");
		}

		final String key = ByteUtil.byteToHex(hash);
		final Integer index = queuedEntryLookUp.containsKey(key) ? queuedEntryLookUp.get(key) : entryLookUp.get(key);
		if (index == null) {
			throw new ArchiveEntryNotFoundException(String.format("No entry found for hash %s", key));
		}
		final StructArchiveEntry entry = entries.get(index.intValue());
		return entry;
	}

	@Override
	public int getNumberOfEntries() {
		return entries.size();
	}

	public final StructPackHeader getPack(byte[] hash) throws ArchiveEntryNotFoundException {
		return getPack(getArchiveEntry(hash));
	}

	@Override
	public boolean hasArchiveData(byte[] hash) {
		final String key = ByteUtil.byteToHex(hash);
		return entryLookUp.containsKey(key) || queuedEntryLookUp.containsKey(key);
	}

	@Override
	public void deleteArchiveData(byte[] hash) throws IOException {
		final String key = ByteUtil.byteToHex(hash);
		final Integer index = queuedEntryLookUp.containsKey(key) ? queuedEntryLookUp.get(key) : entryLookUp.get(key);
		if (index == null) {
			throw new ArchiveEntryNotFoundException(String.format("No entry found for hash %s", key));
		}

		final StructArchiveEntry archiveEntry = entries.get(index);
		final StructPackHeader packHeader = getPack(archiveEntry);

		final MemoryBlock memoryBlock = findMemoryBlock(packHeader.offset);
		freeMemoryBlock(memoryBlock);

		final PackIdxSwap headerIdxSwap = packFile.deletePack(archiveEntry.headerIdx);
		if (headerIdxSwap != null) { // pack idx changed, this means one archiveEntry has now an invalid packIdx. Find the entry and update its packIdx.
			// start with the last element, the change is high, that the last archive entry also uses the last pack
			for (int i = entries.size() - 1; 0 <= i; i--) {
				final StructArchiveEntry aentry = entries.get(i);
				if (aentry.headerIdx == headerIdxSwap.oldPackIdx) {
					aentry.headerIdx = headerIdxSwap.newPackIdx;
					overwriteArchiveEntry(aentry);
					break;
				}
			}
		}

		// if the deleted entry is the last entry, just remove it, otherwise replace deleted entry with last entry
		if ((entries.size() - 1) != index) {
			final StructArchiveEntry lastArchiveEntry = entries.get(entries.size() - 1);
			replaceArchiveEntry(archiveEntry.hash, lastArchiveEntry);
		}
		entries.remove(entries.size() - 1);
	}

	private StructPackHeader getPack(StructArchiveEntry entry) throws ArchiveEntryNotFoundException {
		return getPack(entry.headerIdx);
	}

	private StructPackHeader getArchiveEntryRelatedPack(byte[] hash) {
		return getPack(getArchiveEntry(hash));
	}

	private void loadArchivEntries() throws IOException {
		try (BinaryReader reader = getFileReader()) {
			loadArchivEntries(reader);
		}
	}

	private void loadArchivEntries(BinaryReader reader) {
		final StructRootBlock root = getRootElement();

		final StructPackHeader rootPack = getPack(root.headerIdx);
		reader.seek(Seek.BEGIN, rootPack.getOffset() - Long.BYTES);

		final long blockSize = reader.readInt64();
		archiveArrayCapacity = (int) (blockSize / StructArchiveEntry.SIZE_IN_BYTES);

		entries = new ArrayList<>(root.entryCount);
		for (int i = 0; i < root.entryCount; ++i) {
			final long blockIndex = reader.readUInt32();
			final byte[] hash = new byte[20];
			reader.readInt8(hash, 0, hash.length);
			final long uncompressedSize = reader.readInt64();

			final StructArchiveEntry entry = new StructArchiveEntry(blockIndex, hash, uncompressedSize);
			entries.add(entry);
		}

		entryLookUp = new HashMap<>();
		for (int i = 0; i < entries.size(); i++) {
			final StructArchiveEntry entry = entries.get(i);
			final String hash = ByteUtil.byteToHex(entry.hash);
			if (entryLookUp.containsKey(hash)) {
				throw new ArchiveHashCollisionException();
			}
			entryLookUp.put(hash, i);
		}
	}

	@Override
	public void writeArchiveData(byte[] hash, BinaryReader data) throws IOException {
		if (hasArchiveData(hash)) {
			overwriteArchiveData(hash, data);
		} else {
			writeNewArchiveData(hash, data);
		}
	}

	private void overwriteArchiveData(byte[] hash, BinaryReader data) throws IOException {
		final StructArchiveEntry entry = getArchiveEntry(hash);
		final StructPackHeader pack = getPack(entry);
		final long dataSize = data.size();

		MemoryBlock memoryBlock = findMemoryBlock(pack.getOffset());
		if (memoryBlock.size() < dataSize) {
			freeMemoryBlock(memoryBlock);
			memoryBlock = allocateMemory(dataSize);
		}

		final long dataOffset = writeDataToFile(memoryBlock, data);
		overwritePack(new StructPackHeader(dataOffset, dataSize), entry.headerIdx);
		overwriteArchiveEntry(new StructArchiveEntry(entry.headerIdx, hash, dataSize));
	}

	@Override
	public void replaceArchiveData(byte[] oldHash, byte[] newHash, BinaryReader data) throws IOException {
		final StructArchiveEntry entry = getArchiveEntry(oldHash);
		final StructPackHeader pack = getPack(entry);
		final long dataSize = data.size();

		MemoryBlock memoryBlock = findMemoryBlock(pack.getOffset());
		if (memoryBlock.size() < dataSize) {
			freeMemoryBlock(memoryBlock);
			memoryBlock = allocateMemory(dataSize);
		}

		final long dataOffset = writeDataToFile(memoryBlock, data);
		overwritePack(new StructPackHeader(dataOffset, dataSize), entry.headerIdx);
		replaceArchiveEntry(oldHash, new StructArchiveEntry(entry.headerIdx, newHash, dataSize));
	}

	private void writeNewArchiveData(byte[] hash, BinaryReader data) throws IOException {
		if (hasArchiveData(hash)) {
			throw new ArchiveHashCollisionException();
		}

		final long dataSize = data.size();
		final MemoryBlock memoryBlock = allocateMemory(dataSize);
		final long dataOffset = writeDataToFile(memoryBlock, data);
		final long packIndex = writeNewPack(new StructPackHeader(dataOffset, dataSize));
		writeNewArchiveEntry(new StructArchiveEntry(packIndex, hash, dataSize));
	}

	private long writeDataToFile(MemoryBlock memoryBlock, BinaryReader data) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, memoryBlock.position());
			writer.write(data);
			return memoryBlock.position();
		}
	}

	private int getArchiveArraySize() {
		return entries.size();
	}

	private void computeArchiveArrayCapacity() throws IOException {
		final StructPackHeader pack = getRootPack();
		if (pack.offset == 0) {
			archiveArrayCapacity = 0;
		} else {
			final MemoryBlock block = findMemoryBlock(pack.offset);
			archiveArrayCapacity = block.size() / StructArchiveEntry.SIZE_IN_BYTES;
		}
	}

	@Override
	public void setEstimatedNumberForWriteEntries(int count) throws IOException {
		if (count < 0) {
			throw new IllegalArgumentException("'count' must be greater than or equal 0");
		}
		packFile.setPackArrayMinimalCapacity(count + 2);
		archiveArrayMinimalSize = count;
	}

	@Override
	public void flushWrite() throws IOException {
		flushWrite(true);
	}

	private void flushWrite(boolean flushSub) throws IOException {
		getRootElement().entryCount = entries.size();

		if (packFile.isWriteModeEnabled()) {
			final int size = Math.max(archiveArrayMinimalSize, entries.size());
			if (getArchiveArrayCapacity() < size) {
				setArchiveArrayCapacityTo(size);
			}

			final StructPackHeader pack = getRootPack();
			for (final Integer index : queuedEntryLookUp.values()) {
				final long offset = pack.offset + (index * StructArchiveEntry.SIZE_IN_BYTES);
				final StructArchiveEntry entry = entries.get(index);
				writeArchiveEntryToFileAt(offset, entry);
			}

			queuedEntryLookUp.clear();
		}

		if (flushSub) {
			super.flushWrite();
		}
	}

	private void writeArchiveEntryToFile(int index, StructArchiveEntry entry) throws IOException {
		final StructPackHeader pack = getRootPack();
		final long offset = pack.offset + (index * StructArchiveEntry.SIZE_IN_BYTES);
		writeArchiveEntryToFileAt(offset, entry);
	}

	private void writeArchiveEntryToFileAt(long offset, StructArchiveEntry entry) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, offset);
			writer.writeInt32(entry.headerIdx);
			writer.writeInt8(entry.hash, 0, 20);
			writer.writeInt64(entry.size);
		}
	}

	private void setArchiveArrayCapacityTo(int size) throws IOException {
		if (isArchiveArrayInitialized()) {
			increaseArchiveArrayCapacity(Math.max(getArchiveArrayCapacity(), size));
		} else {
			allocateInitialArchiveArray(size);
		}
	}

	private void increaseArchiveArrayCapacity(int newCapacity) throws IOException {
		if (newCapacity < archiveArrayCapacity) {
			throw new IllegalArgumentException("'new capacity' must be greater or equal to current capacity");
		}

		if (newCapacity == archiveArrayCapacity) {
			return;
		}

		final StructRootBlock rootElement = getRootElement();
		final StructPackHeader pack = getPack(rootElement.headerIdx);

		final long oldSize = getArchiveArraySize() * StructArchiveEntry.SIZE_IN_BYTES;
		final long newSize = newCapacity * StructArchiveEntry.SIZE_IN_BYTES;

		try (BinaryWriter writer = getFileWriter()) {
			final long newOffset = relocateMemory(writer, pack.offset, oldSize, newSize);
			pack.size = newSize;
			pack.offset = newOffset;
		}
		overwritePack(pack, rootElement.headerIdx);

		computeArchiveArrayCapacity();
	}

	private long relocateMemory(BinaryWriter writer, long memOffset, long memOldSize, long memNewSize) throws IOException {
		final MemoryBlock oldBlock = findMemoryBlock(memOffset);
		final ByteBuffer temp = ByteBuffer.allocateDirect((int) memOldSize);
		try (BinaryReader reader = getFileReader()) {
			reader.seek(Seek.BEGIN, oldBlock.position());
			reader.readTo(temp);
			temp.flip();
		}

		freeMemoryBlock(oldBlock);

		final MemoryBlock newBlock = allocateMemory(memNewSize);
		writer.seek(Seek.BEGIN, newBlock.position());
		writer.write(temp);
		return newBlock.position();
	}

	private int getArchiveArrayCapacity() {
		return archiveArrayCapacity;
	}

	private boolean isArchiveArrayInitialized() {
		final StructPackHeader pack = getRootPack();
		return pack.offset != 0;
	}

	private void allocateInitialArchiveArray(int size) throws IOException {
		final StructRootBlock rootElement = getRootElement();
		final StructPackHeader pack = getPack(rootElement.headerIdx);
		final MemoryBlock block = allocateMemory(size * StructArchiveEntry.SIZE_IN_BYTES);
		pack.offset = block.position();
		pack.size = block.size();
		overwritePack(pack, rootElement.headerIdx);
		computeArchiveArrayCapacity();
	}

	@Override
	protected void beforeFileClose() throws IOException {
		flushWrite(false);
		entryLookUp.clear();
		queuedEntryLookUp.clear();
		entries.clear();
	}

	private void writeNewArchiveEntry(StructArchiveEntry entry) throws IOException {
		final String key = ByteUtil.byteToHex(entry.hash);
		final Integer index = Integer.valueOf(entries.size());
		entries.add(entry);

		if (getArchiveArraySize() < getArchiveArrayCapacity()) {
			entryLookUp.put(key, index);
			writeArchiveEntryToFile(index, entry);
		} else {
			queuedEntryLookUp.put(key, index);
		}
	}

	private void overwriteArchiveEntry(StructArchiveEntry entry) throws IOException {
		final String key = ByteUtil.byteToHex(entry.hash);

		if (queuedEntryLookUp.containsKey(key)) {
			final Integer index = queuedEntryLookUp.get(key);
			entries.set(index, entry);
		} else {
			final Integer index = entryLookUp.get(key);
			final StructPackHeader rootPack = getRootPack();
			final long offset = rootPack.getOffset() + (index * StructArchiveEntry.SIZE_IN_BYTES);
			entries.set(index, entry);
			writeArchiveEntryToFileAt(offset, entry);
		}
	}

	private void replaceArchiveEntry(byte[] oldHash, StructArchiveEntry entry) throws IOException {
		final String oldKey = ByteUtil.byteToHex(oldHash);
		final String newKey = ByteUtil.byteToHex(entry.hash);

		if (queuedEntryLookUp.containsKey(oldKey)) {
			final Integer index = queuedEntryLookUp.get(oldKey);
			entries.set(index, entry);

			queuedEntryLookUp.remove(oldKey);
			queuedEntryLookUp.put(newKey, index);
		} else {
			final Integer index = entryLookUp.get(oldKey);
			final StructPackHeader rootPack = getRootPack();
			final long offset = rootPack.getOffset() + (index * StructArchiveEntry.SIZE_IN_BYTES);
			entries.set(index, entry);
			writeArchiveEntryToFileAt(offset, entry);

			entryLookUp.remove(oldKey);
			entryLookUp.put(newKey, index);
		}
	}

}
