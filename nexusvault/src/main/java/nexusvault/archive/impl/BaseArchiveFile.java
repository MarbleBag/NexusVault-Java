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
import nexusvault.archive.struct.StructArchiveEntry;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.shared.exception.IntegerOverflowException;

final class BaseArchiveFile extends AbstractPackedFile implements ArchiveFile {

	private Map<String, Integer> entryLookUp;
	private List<StructArchiveEntry> entries;

	private int maxEntryCount;

	@Override
	public BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException {
		try (BinaryReader reader = getFileReader()) {
			final StructPackHeader pack = getPack(hash);

			reader.seek(Seek.BEGIN, pack.getOffset());

			if ((pack.size > Integer.MAX_VALUE) || (pack.size < 0)) {
				throw new IntegerOverflowException("Archive file: data exceeds " + Integer.MAX_VALUE + " bytes");
			}

			final ByteBuffer data = ByteBuffer.allocate((int) pack.size).order(reader.getOrder());
			final int writtenData = reader.readTo(data);
			data.flip();

			if (writtenData != data.capacity()) {
				throw new ArchiveException("Data not complete");
			}

			return new ByteBufferBinaryReader(data);
		}
	}

	public StructArchiveEntry getArchiveEntry(byte[] hash) throws ArchiveEntryNotFoundException {
		if (hash == null) {
			throw new IllegalArgumentException("'entry' must not be null");
		}
		final String key = ByteUtil.byteToHex(hash);
		final Integer index = entryLookUp.get(key);
		if (index == null) {
			throw new IllegalStateException(); // TODO
		}
		final StructArchiveEntry entry = entries.get(index);

		if (entry == null) {
			throw new ArchiveEntryNotFoundException(key);
		}
		return entry;
	}

	@Override
	public int getNumberOfEntries() {
		return rootElement.entryCount;
	}

	public final StructPackHeader getPack(byte[] hash) throws ArchiveEntryNotFoundException {
		return getPack(getArchiveEntry(hash));
	}

	@Override
	public boolean hasArchiveData(byte[] hash) {
		return entryLookUp.containsKey(ByteUtil.byteToHex(hash));
	}

	@Override
	public void setArchiveData(byte[] hash, BinaryReader data) throws IOException {
		if (!hasArchiveData(hash)) {
			writeNewArchiveData(hash, data);
		} else {
			overwriteArchiveData(hash, data);
		}
	}

	@Override
	public void setNumberOfExpectedEntries(int count) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	private final StructPackHeader getPack(StructArchiveEntry entry) throws ArchiveEntryNotFoundException {
		return getPack(entry.headerIdx);
	}

	private void loadArchivEntries(BinaryReader reader) {
		final StructPackHeader rootPack = getPack(rootElement.headerIdx);
		reader.seek(Seek.BEGIN, rootPack.getOffset());

		entries = new ArrayList<>(rootElement.entryCount);
		for (int i = 0; i < rootElement.entryCount; ++i) {
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
		final StructPackHeader pack = getPack((int) entry.headerIdx);
		final MemoryBlock block = memoryModel.tryFindBlockAt(pack.offset);
		if (block == null) {
			throw new IllegalStateException(); // TODO
		}

		final long dataSize = data.size();
		if (block.size() >= dataSize) { // TODO maybe resize
			try (BinaryWriter writer = getFileWriter()) {
				final long dataOffset = writeDataToBlock(writer, block, data);
				overwritePack(writer, entry.headerIdx, new StructPackHeader(dataOffset, dataSize));
				overwriteArchiveEntry(writer, new StructArchiveEntry(entry.headerIdx, hash, dataSize));
			}
		} else {
			memoryModel.freeMemory(block);
			final MemoryBlock newBlock = memoryModel.allocateMemory(dataSize);
			try (BinaryWriter writer = getFileWriter()) {
				final long dataOffset = writeDataToBlock(writer, newBlock, data);
				overwritePack(writer, entry.headerIdx, new StructPackHeader(dataOffset, dataSize));
				overwriteArchiveEntry(writer, new StructArchiveEntry(entry.headerIdx, hash, dataSize));
				updateMemoryLayout(writer);
			}
		}
	}

	private void overwriteArchiveEntry(BinaryWriter writer, StructArchiveEntry entry) {
		final Integer index = entryLookUp.get(ByteUtil.byteToHex(entry.hash));
		if (index == null) {
			throw new IllegalStateException(); // TODO
		}

		final StructPackHeader rootPack = getPack(rootElement.headerIdx);
		final long offset = rootPack.getOffset() + (index * StructArchiveEntry.SIZE_IN_BYTES);
		writer.seek(Seek.BEGIN, offset);
		writer.writeInt32(entry.headerIdx);
		writer.writeInt8From(entry.hash, 0, 20);
		writer.writeInt64(entry.size);
		entries.set(index.intValue(), entry);
	}

	private void writeNewArchiveData(byte[] hash, BinaryReader data) throws IOException {
		if (true) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		final long dataSize = data.size();
		final MemoryBlock block = memoryModel.allocateMemory(dataSize);
		try (BinaryWriter writer = getFileWriter()) {
			final long dataOffset = writeDataToBlock(writer, block, data);
			final long packIdx = writeNewPack(writer, new StructPackHeader(dataOffset, dataSize));
			writeNewArchiveEntry(writer, new StructArchiveEntry(packIdx, hash, dataSize));
			updateMemoryLayout(writer);
		}
	}

	private void writeNewArchiveEntry(BinaryWriter writer, StructArchiveEntry entry) throws IOException {
		if (true) {
			throw new UnsupportedOperationException("Not implemented yet");
		}

		if (entries.size() == maxEntryCount) {
			// TODO rellocate entries
		}

		// writer.seek(Seek.BEGIN, archiveEntryPosition + (entries.size() * StructArchiveEntry.SIZE_IN_BYTES));
		writer.writeInt32(entry.headerIdx);
		writer.writeInt8From(entry.hash, 0, entry.hash.length);
		writer.writeInt64(entry.size);

		// entries.put(ByteUtil.byteToHex(entry.hash), entry);
	}

	protected final void computeMaxEntryCount(BinaryReader reader) {
		final StructPackHeader rootPack = getPack(rootElement.headerIdx);
		final MemoryBlock block = memoryModel.tryFindBlockAt(rootPack.offset);
		if (block == null) {
			throw new IllegalStateException(); // TODO
		}
		this.maxEntryCount = block.size() / StructArchiveEntry.SIZE_IN_BYTES;

		if (this.maxEntryCount < entries.size()) {
			throw new IllegalStateException(); // TODO
		}
	}

	@Override
	protected void readExistingFile(BinaryReader reader) {
		super.readExistingFile(reader);
		loadArchivEntries(reader);
	}

}
