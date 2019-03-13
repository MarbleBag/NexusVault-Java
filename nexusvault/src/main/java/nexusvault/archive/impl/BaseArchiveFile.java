package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.ArchiveException;
import nexusvault.archive.ArchiveHashCollisionException;
import nexusvault.archive.impl.ArchiveMemoryModel.MemoryBlock;
import nexusvault.archive.struct.StructAARC;
import nexusvault.archive.struct.StructArchiveEntry;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.archive.struct.StructRootBlock;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;

final class BaseArchiveFile extends AbstractArchiveFile implements ArchiveFile {

	private Map<String, Integer> entryLookUp;
	private Map<String, Integer> queuedEntryLookUp;
	private List<StructArchiveEntry> entries;

	private int archiveArrayMinimalSize;
	private int archiveArrayCapacity;

	public BaseArchiveFile() {
		super();
		entryLookUp = new HashMap<String, Integer>();
		queuedEntryLookUp = new HashMap<String, Integer>();
		entries = new ArrayList<StructArchiveEntry>();
	}

	@Override
	protected void afterFileRead(boolean isFileNew) throws IOException {
		archiveArrayCapacity = 0;

		if (isFileNew) {
			enableWriteMode();
			packFile.setPackArrayAutoGrowSize(1000);
			packFile.setPackArrayAutoGrow(true);

			final long emptyPack = writeNewPack(new StructPackHeader());
			final long archiveEntryPack = writeNewPack(new StructPackHeader());

			final StructRootBlock aarc = new StructRootBlock(StructRootBlock.SIGNATURE_AARC, 1, 0, (int) archiveEntryPack);
			final StructPackHeader pack = writeRootElement(aarc);
			final long aidxIndex = writeNewPack(pack);
			setPackRootIdx(aidxIndex);
			
			computeArchiveArrayCapacity();
		} else {
			final StructRootBlock aarc = getRootElement();
			if (aarc == null)
				throw new IllegalStateException(); // TODO
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
		if (hash == null)
			throw new IllegalArgumentException("'entry' must not be null");

		final String key = ByteUtil.byteToHex(hash);
		Integer index = queuedEntryLookUp.containsKey(key) ? queuedEntryLookUp.get(key) : entryLookUp.get(key);
		if (index == null)
			throw new ArchiveEntryNotFoundException(String.format("No entry found for hash %s", key));
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
		return entryLookUp.containsKey(ByteUtil.byteToHex(hash));
	}

	@Override
	public void writeArchiveData(byte[] hash, BinaryReader data) throws IOException {
		if (hasArchiveData(hash)) {
			overwriteArchiveData(hash, data);
		} else {
			writeNewArchiveData(hash, data);
		}
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
		reader.seek(Seek.BEGIN, rootPack.getOffset()-Long.BYTES);

		long blockSize = reader.readInt64();
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

	private void overwriteArchiveData(byte[] hash, BinaryReader data) throws IOException {
		final StructArchiveEntry entry = getArchiveEntry(hash);
		final StructPackHeader pack = getPack(entry);
		long dataSize = data.size();

		MemoryBlock memoryBlock = findMemoryBlock(pack.getOffset());
		if (memoryBlock.size() < dataSize) {
			freeMemoryBlock(memoryBlock);
			memoryBlock = allocateMemory(dataSize);
		}

		final long dataOffset = writeDataToFile(memoryBlock, data);
		overwritePack(new StructPackHeader(dataOffset, dataSize), entry.headerIdx);
		overwriteArchiveEntry(new StructArchiveEntry(entry.headerIdx, hash, dataSize));
	}

	protected void overwriteArchiveEntry(StructArchiveEntry entry) throws IOException {
		final String key = ByteUtil.byteToHex(entry.hash);
		if (queuedEntryLookUp.containsKey(key)) {
			Integer index = queuedEntryLookUp.get(key);
			entries.set(index, entry);
		} else {
			final Integer index = entryLookUp.get(key);
			final StructPackHeader rootPack = getRootPack();
			final long offset = rootPack.getOffset() + (index * StructArchiveEntry.SIZE_IN_BYTES);
			entries.set(index, entry);
			writeArchiveEntryToFileAt(offset, entry);
		}
	}

	private void writeNewArchiveData(byte[] hash, BinaryReader data) throws IOException {
		long dataSize = data.size();
		MemoryBlock memoryBlock = allocateMemory(dataSize);
		final long dataOffset = writeDataToFile(memoryBlock, data);
		long packIndex = writeNewPack(new StructPackHeader(dataOffset, dataSize));
		writeNewArchiveEntry(new StructArchiveEntry(packIndex, hash, dataSize));
	}

	private long writeDataToFile(MemoryBlock memoryBlock, BinaryReader data) throws IOException {
		try (BinaryWriter writer = getFileWriter()) {
			writer.seek(Seek.BEGIN, memoryBlock.position());
			writer.write(data);
			return memoryBlock.position();
		}
	}

	private void writeNewArchiveEntry(StructArchiveEntry entry) throws IOException {
		final String key = ByteUtil.byteToHex(entry.hash);

		if (entryLookUp.containsKey(key) || queuedEntryLookUp.containsKey(key))
			throw new ArchiveHashCollisionException();

		Integer index = Integer.valueOf(entries.size());
		entries.add(entry);

		if (getArchiveArraySize() < getArchiveArrayCapacity()) {
			entryLookUp.put(key, index);
			writeArchiveEntryToFile(index, entry);
		} else {
			queuedEntryLookUp.put(key, index);
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
		if (count < 0)
			throw new IllegalArgumentException("'count' must be greater than or equal 0");
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

			StructPackHeader pack = getRootPack();
			for (Integer index : queuedEntryLookUp.values()) {
				long offset = pack.offset + index * StructArchiveEntry.SIZE_IN_BYTES;
				StructArchiveEntry entry = entries.get(index);
				writeArchiveEntryToFileAt(offset, entry);
			}

			queuedEntryLookUp.clear();
		}

		if (flushSub)
			super.flushWrite();
	}

	private void writeArchiveEntryToFile(int index, StructArchiveEntry entry) throws IOException {
		StructPackHeader pack = getRootPack();
		long offset = pack.offset + index * StructArchiveEntry.SIZE_IN_BYTES;
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

		StructRootBlock rootElement = getRootElement();
		StructPackHeader pack = getPack(rootElement.headerIdx);

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
		StructPackHeader pack = getRootPack();
		return pack.offset != 0;
	}

	private void allocateInitialArchiveArray(int size) throws IOException {
		StructRootBlock rootElement = getRootElement();
		StructPackHeader pack = getPack(rootElement.headerIdx);
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

}
