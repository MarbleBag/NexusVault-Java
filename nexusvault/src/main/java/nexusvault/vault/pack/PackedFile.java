package nexusvault.vault.pack;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import kreed.io.util.BinaryIOException;
import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryReaderView;
import kreed.io.util.BinaryWriter;
import kreed.io.util.BinaryWriterCounter;
import kreed.io.util.BinaryWriterView;
import kreed.io.util.ByteAlignmentUtil;
import kreed.io.util.Seek;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;
import nexusvault.vault.FileClosedIOException;
import nexusvault.vault.file.BufferedFileAccess;
import nexusvault.vault.pack.PackException.PackIndexCollisionException;
import nexusvault.vault.pack.PackException.PackIndexInvalidException;
import nexusvault.vault.pack.PackException.PackMalformedException;
import nexusvault.vault.struct.StructPackEntry;
import nexusvault.vault.struct.StructPackFileHeader;

// TODO deletion is incomplete
// TODO merge and split of file regions to not waste space
public final class PackedFile implements Closeable {

	private static final int SIGNATURE = StructPackFileHeader.SIGNATURE;
	private static final int VERSION = 1;

	private static final int MINIMUM_GROW_VOLUME = 50;
	private static final float GROW_FACTOR = 1.25f;

	private static final class IndexEntry {
		long offset;
		long size;
		long capacity;

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("IndexEntry [offset=").append(this.offset);
			builder.append(", size=").append(this.size);
			builder.append(", capacity=").append(this.capacity);
			builder.append("]");
			return builder.toString();
		}
	}

	private final List<IndexEntry> indexTable = new ArrayList<>();
	private final Set<Integer> unusedIndices = new HashSet<>();
	private final Set<Integer> freeEntry = new HashSet<>();
	private final Set<Integer> deletableEntry = new HashSet<>();
	private int tableIndex;

	private final BufferedFileAccess file = new BufferedFileAccess();

	private StructPackFileHeader fileHeader = null;

	private boolean isFileOpen;
	private boolean writeAppendingInProgress;
	private boolean dirty;

	private Path getPath() {
		return this.file.getPath();
	}

	public void open(Path path) throws IOException, PackException {
		Objects.requireNonNull(path, "'path' must not be null");
		if (path.equals(getPath())) {
			return; // nothing to do
		}

		if (this.isFileOpen) {
			close();
		}

		final var doesFileExist = Files.exists(path);
		this.file.open(path, EnumSet.of(StandardOpenOption.CREATE));

		try {
			if (doesFileExist) {
				loadFileContent();
			} else {
				this.fileHeader = new StructPackFileHeader();
				this.fileHeader.signature = SIGNATURE;
				this.fileHeader.version = VERSION;
				this.fileHeader.endOfFile = StructPackFileHeader.SIZE_IN_BYTES + Long.BYTES;
				this.indexTable.add(new IndexEntry());
				this.dirty = true;
			}
			this.isFileOpen = true;
		} catch (final Exception e) {
			close();
			throw e;
		}
	}

	public boolean isOpen() {
		return this.isFileOpen;
	}

	private void assertFileIsOpen() throws IOException {
		if (!isOpen()) {
			throw new FileClosedIOException();
		}
	}

	private void loadFileContent() throws IOException, PackException {
		final var indexLookup = new TreeMap<Long, Integer>(); // offset, index

		try (var reader = this.file.getFileReader()) {
			{ // load and validate header
				this.fileHeader = new StructPackFileHeader(reader);

				if (this.fileHeader.signature != SIGNATURE) {
					throw new SignatureMismatchException("Packed file", SIGNATURE, this.fileHeader.signature);
				}

				if (this.fileHeader.version != VERSION) {
					throw new VersionMismatchException("Packed file", VERSION, this.fileHeader.version);
				}
			}

			// load and validate index table
			if (this.fileHeader.indexOffset > 0) {
				final var minValidOffset = StructPackFileHeader.SIZE_IN_BYTES + Long.BYTES;

				reader.seek(Seek.BEGIN, this.fileHeader.indexOffset);
				if (this.fileHeader.indexCount > Integer.MAX_VALUE) {
					throw new IntegerOverflowException();
				}

				this.indexTable.clear();
				final var entryCount = this.fileHeader.indexCount;
				for (int index = 0; index < entryCount; ++index) {
					final var entry = new IndexEntry();
					entry.offset = reader.readInt64();
					entry.size = reader.readInt64();
					this.indexTable.add(entry);

					if (entry.offset <= minValidOffset) {
						entry.size = 0;
						entry.offset = 0;
						this.unusedIndices.add(index);
					}

					if (entry.offset != 0) {
						final var oldIndex = indexLookup.put(entry.offset, index);
						if (oldIndex != null) {
							throw new PackIndexCollisionException(oldIndex, index, entry.offset);
						}
					}

					if (entry.offset == this.fileHeader.indexOffset) {
						this.tableIndex = index;
					}
				}
			}

			if (!this.unusedIndices.contains(0)) {
				throw new PackMalformedException("Missing empty entry at 0");
			} else {
				this.unusedIndices.remove(0);
			}

		}
	}

	public void validateFile() throws BinaryIOException, IOException {
		final var fileLayout = new TreeMap<Long, Long>(); // offset, size

		try (var reader = this.file.getFileReader()) {
			// load and validate file layout
			reader.seek(Seek.BEGIN, StructPackFileHeader.SIZE_IN_BYTES + Long.BYTES); // + first guard, ignore
			while (!reader.isEndOfData()) {
				final long size = reader.readInt64(); // can be negative
				if (size == 0) {
					break;
				}
				if (Math.abs(size) != ByteAlignmentUtil.alignTo16Byte(Math.abs(size))) {
					throw new PackMalformedException(String.format("'size' must be a multiple of 16. Was %d", size));
				}

				final long offset = reader.position();
				if (offset != ByteAlignmentUtil.alignTo16Byte(offset)) {
					throw new PackMalformedException("'offset' must be aligned to a 16-byte boundary");
				}

				fileLayout.put(offset, size);
				reader.seek(Seek.CURRENT, Math.abs(size) + Long.BYTES);
			}
		}

		for (int index = 1; index < this.fileHeader.indexCount; index++) {
			if (this.unusedIndices.contains(index)) {
				continue;
			}

			final var entry = this.indexTable.get(index);
			final var guard = fileLayout.get(entry.offset);
			if (guard == null) { // entry points to no valid file region
				entry.offset = 0;
				entry.size = 0;
				this.unusedIndices.add(index);
				continue;
			}

			final var guardValue = guard.longValue();
			entry.capacity = Math.abs(guardValue);
			if (entry.size > entry.capacity) {
				throw new PackMalformedException(String.format("Invalid 'size' of entry %d", index));
			}

			if (guardValue < 0) {
				this.freeEntry.add(index);
			}
		}

		final var indexLookup = new TreeMap<Long, Integer>(); // offset, index
		for (var index = 0; index < this.indexTable.size(); ++index) {
			final var entry = this.indexTable.get(index);
			if (entry.offset == 0) {
				continue;
			}
			indexLookup.put(entry.offset, index);
		}

		for (final var region : fileLayout.entrySet()) {
			if (!indexLookup.containsKey(region.getKey())) {
				final var index = getNextTableIndex();
				this.freeEntry.add(index);

				final var entry = this.indexTable.get(index);
				entry.offset = region.getKey();
				entry.capacity = region.getValue();
				entry.size = entry.capacity;
			}
		}

		if (this.unusedIndices.contains(this.tableIndex)) {
			throw new PackMalformedException("index is stored in invalid area");
		}
	}

	public void flush() throws BinaryIOException, IOException {
		if (this.indexTable.size() > 0) {
			final var newCapacity = (this.indexTable.size() + (int) Math.max(MINIMUM_GROW_VOLUME, this.indexTable.size() * GROW_FACTOR))
					* StructPackEntry.SIZE_IN_BYTES;

			if (this.fileHeader.indexOffset == 0) {
				this.tableIndex = createNewEntry(newCapacity);
			} else {
				assertIndexIsValid(this.tableIndex);
				final var entry = this.indexTable.get(this.tableIndex);
				updateCapacity(entry);
				final var expectedSize = this.indexTable.size() * StructPackEntry.SIZE_IN_BYTES;
				if (entry.capacity < expectedSize) {
					releaseEntry(this.tableIndex);
					this.tableIndex = createNewEntry(newCapacity);
				}
			}

			final var entry = this.indexTable.get(this.tableIndex);
			entry.size = this.indexTable.size() * StructPackEntry.SIZE_IN_BYTES;
		}

		this.fileHeader.indexCount = this.indexTable.size();
		this.fileHeader.indexOffset = this.indexTable.get(this.tableIndex).offset;

		try (final var writer = this.file.getFileWriter()) {
			if (this.fileHeader.indexCount > 0) {
				writer.seek(Seek.BEGIN, this.fileHeader.indexOffset);
				for (final var entry : this.indexTable) {
					writer.writeInt64(entry.offset);
					writer.writeInt64(entry.size);
				}
			}

			long lastWrittenPosition = 0;
			for (int index = 1; index < this.indexTable.size(); index++) {
				final var indexEntry = this.indexTable.get(index);
				if (this.unusedIndices.contains(index)) {
					continue;
				}

				final var guard = this.freeEntry.contains(index) ? -indexEntry.capacity : indexEntry.capacity;
				writer.seek(Seek.BEGIN, indexEntry.offset - Long.BYTES);
				writer.writeInt64(guard);
				writer.seek(Seek.BEGIN, indexEntry.offset + indexEntry.capacity);
				writer.writeInt64(guard);

				if (lastWrittenPosition < writer.position()) {
					lastWrittenPosition = writer.position();
				}
			}

			this.fileHeader.endOfFile = lastWrittenPosition;
			writer.seek(Seek.BEGIN, 0);
			this.fileHeader.write(writer);
		}

		this.dirty = false;

		// TODO delete entries in #deletableEntry
	}

	private int getNextTableIndex() throws IntegerOverflowException {
		if (this.unusedIndices.isEmpty()) {
			final var index = this.indexTable.size();
			this.indexTable.add(new IndexEntry());
			if (index > Integer.MAX_VALUE) {
				throw new IntegerOverflowException();
			}
			return index;
		} else { // reuse old index
			final var it = this.unusedIndices.iterator();
			final var index = it.next();
			it.remove();
			return index;
		}
	}

	private void assertIndexIsValid(long index) {
		if (index <= 0 && this.indexTable.size() <= index) {
			throw new PackIndexInvalidException(index); // TODO
		}
		if (this.unusedIndices.contains((int) index)) {
			throw new PackIndexInvalidException(index); // TODO
		}
	}

	private void assertIndexIsClaimed(long index) {
		if (this.freeEntry.contains((int) index)) {
			throw new PackIndexInvalidException(index); // TODO
		}
	}

	private void assertIndexIsNotTable(long index) {
		if (this.tableIndex != 0 && this.tableIndex == index) {
			throw new PackIndexInvalidException(index); // TODO
		}
	}

	private void updateCapacity(IndexEntry entry) throws BinaryIOException, IOException {
		if (entry.capacity != 0 || entry.offset == 0) {
			return;
		}

		try (var reader = this.file.getFileReader()) {
			reader.seek(Seek.BEGIN, entry.offset - Long.BYTES);
			final var capacity = reader.readInt64();
			entry.capacity = Math.abs(capacity);
		}
	}

	@Override
	public void close() throws IOException {
		close(this.dirty);
	}

	private void close(boolean writeToFile) throws IOException {
		if (writeToFile) {
			flush();
		}

		this.fileHeader = null;
		this.indexTable.clear();
		this.unusedIndices.clear();
		this.freeEntry.clear();
		this.tableIndex = 0;

		this.writeAppendingInProgress = false;
		this.isFileOpen = false;
		this.dirty = false;

		this.file.close();
	}

	public long getRootIndex() throws IOException {
		assertFileIsOpen();
		return this.fileHeader.rootEntryIdx;
	}

	public void setRootIndex(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsClaimed(index);
		assertIndexIsNotTable(index);
		this.fileHeader.rootEntryIdx = index;
		this.dirty = true;
	}

	public BinaryReader readEntry(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsClaimed(index);
		assertIndexIsNotTable(index);
		final var entry = this.indexTable.get((int) index);
		return new BinaryReaderView(this.file.getFileReader(), entry.offset, entry.size, true);
	}

	public byte[] readEntry(long index, byte[] data, int offset, int maxBytes) throws BinaryIOException, IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsClaimed(index);
		assertIndexIsNotTable(index);

		final var entry = this.indexTable.get((int) index);
		maxBytes = (int) Math.min(entry.size, maxBytes);

		if (data == null) {
			data = new byte[maxBytes];
			offset = 0;
		}

		final var length = Math.min(data.length - offset, maxBytes);
		try (var reader = this.file.getFileReader()) {
			reader.seek(Seek.BEGIN, entry.offset);
			reader.readInt8(data, offset, length);
		}

		return data;
	}

	public BinaryWriter writeEntry(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsClaimed(index);
		assertIndexIsNotTable(index);
		final var entry = this.indexTable.get((int) index);
		updateCapacity(entry);
		return new BinaryWriterCounter(new BinaryWriterView(this.file.getFileWriter(), entry.offset, entry.capacity, true)) {
			@Override
			public void close() throws IOException {
				super.close();
				if (this.writtenBytes > 0) {
					PackedFile.this.dirty = true;
					entry.size = this.writeSize;
				}
			}
		};
	}

	public void writeEntry(long index, byte[] data, int offset, int length) throws BinaryIOException, IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsClaimed(index);
		assertIndexIsNotTable(index);

		final var entry = this.indexTable.get((int) index);
		updateCapacity(entry);

		length = (int) Math.min(entry.capacity, Math.min(data.length - offset, length));
		entry.size = length;

		try (var writer = this.file.getFileWriter()) {
			writer.seek(Seek.BEGIN, entry.offset);
			writer.writeInt8(data, offset, length);
		}

		this.dirty = true;
	}

	public long newEntry(long maxSize) throws IOException {
		assertFileIsOpen();
		return createNewEntry(maxSize);
	}

	private int searchFreeEntry(long maxSize) throws BinaryIOException, IOException {
		int index = 0;
		long prevCapacity = Long.MAX_VALUE;

		for (final var idx : this.freeEntry) {
			final var entry = this.indexTable.get(idx);
			updateCapacity(entry);

			if (maxSize <= entry.capacity && entry.capacity < prevCapacity) {
				index = idx;
				prevCapacity = entry.capacity;
			}
		}

		this.freeEntry.remove(index);
		this.deletableEntry.remove(index);
		return index;
	}

	private int createNewEntry(long maxSize) throws IOException {
		{ // find an unused entry with at least 'capacity' bytes
			final int index = searchFreeEntry(maxSize);
			if (index > 0) {
				return index;
			}
		}

		if (this.writeAppendingInProgress) {
			throw new IOException(); // TODO
		}

		final var index = getNextTableIndex();
		final var entry = this.indexTable.get(index);
		entry.capacity = ByteAlignmentUtil.alignTo16Byte(maxSize);
		entry.offset = ByteAlignmentUtil.alignTo16Byte(this.fileHeader.endOfFile);
		entry.size = 0;

		try (var writer = this.file.getFileWriter()) {
			this.dirty = true;
			writer.seek(Seek.BEGIN, entry.offset - Long.BYTES);
			writer.writeInt64(entry.capacity);
			writer.seek(Seek.BEGIN, entry.offset + entry.capacity);
			writer.writeInt64(entry.capacity);
			this.fileHeader.endOfFile = writer.position();
		}

		return index;
	}

	public long writeNewEntry(Consumer<BinaryWriter> consumer) throws IOException {
		assertFileIsOpen();
		if (this.writeAppendingInProgress) {
			throw new IOException(); // TODO
		}
		this.writeAppendingInProgress = true;
		final var offset = ByteAlignmentUtil.alignTo16Byte(this.fileHeader.endOfFile);

		final AtomicInteger index = new AtomicInteger(0);

		final var writer = new BinaryWriterCounter(new BinaryWriterView(this.file.getFileWriter(), offset, Long.MAX_VALUE, true)) {
			@Override
			public void close() throws IOException {
				super.close();
				PackedFile.this.writeAppendingInProgress = false;
				if (this.writeSize == 0) {
					return; // done
				}

				final var idx = getNextTableIndex();
				final var entry = PackedFile.this.indexTable.get(idx);
				entry.offset = offset;
				entry.size = this.writeSize;
				entry.capacity = ByteAlignmentUtil.alignTo16Byte(entry.size);
				index.set(idx);

				try (var writer = PackedFile.this.file.getFileWriter()) {
					PackedFile.this.dirty = true;
					writer.seek(Seek.BEGIN, entry.offset - Long.BYTES);
					writer.writeInt64(entry.capacity);
					writer.seek(Seek.BEGIN, entry.offset + entry.capacity);
					writer.writeInt64(entry.capacity);
					PackedFile.this.fileHeader.endOfFile = writer.position();
				}
			}
		};

		try (writer) {
			consumer.accept(writer);
		}

		return index.longValue();
	}

	public void releaseEntry(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsNotTable(index);
		this.freeEntry.add((int) index);
		this.dirty = true;
	}

	public void claimEntry(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsNotTable(index);
		this.freeEntry.remove((int) index);
		this.dirty = true;
	}

	public Set<Long> getEntries() throws IOException {
		assertFileIsOpen();
		final var set = new HashSet<Long>();
		for (var index = 1; index < this.indexTable.size(); ++index) {
			if (this.unusedIndices.contains(index)) {
				continue;
			}
			set.add((long) index);
		}

		set.remove((long) this.tableIndex);
		return set;
	}

	/**
	 * Returns the maximal capacity in bytes of entry at the given index. It's not possible to write more bytes into an entry than it's capacity allows.
	 *
	 * @param index
	 *            of entry
	 * @return the maximal capacity (in bytes)
	 * @throws IOException
	 */
	public long entryCapacity(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		final var entry = this.indexTable.get((int) index);
		updateCapacity(entry);
		return entry.capacity;
	}

	public long entrySize(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		return this.indexTable.get((int) index).size;
	}

	public boolean isIndexValid(long index) {
		if (index <= 0 && this.indexTable.size() <= index) {
			return false;
		}
		if (this.unusedIndices.contains((int) index)) {
			return false;
		}
		return true;
	}

	public Set<Long> getUnclaimedEntries() throws IOException {
		return this.freeEntry.stream().map(Integer::longValue).collect(Collectors.toSet());
	}

	public void deleteEntry(long index) throws IOException {
		assertFileIsOpen();
		assertIndexIsValid(index);
		assertIndexIsNotTable(index);
		this.freeEntry.add((int) index);
		this.deletableEntry.add((int) index);
		this.dirty = true;
	}

}
