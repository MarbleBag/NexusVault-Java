package nexusvault.vault.archive;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kreed.io.util.BinaryIOException;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;
import nexusvault.vault.FileClosedIOException;
import nexusvault.vault.archive.ArchiveException.ArchiveHashCollisionException;
import nexusvault.vault.archive.ArchiveException.ArchiveHashNotFoundException;
import nexusvault.vault.pack.PackedFile;
import nexusvault.vault.struct.StructArchiveEntry;
import nexusvault.vault.struct.StructArchiveRootElement;

public final class PackedArchiveFile implements Closeable {

	private static final int SIGNATURE = StructArchiveRootElement.SIGNATURE_AARC;
	private static final int VERSION = 2;

	private static final class HashKey {
		private final byte[] value;
		private final int computed;

		public HashKey(byte[] hash) {
			this.value = Objects.requireNonNull(hash, "argument: 'hash'");
			this.computed = 31 + Arrays.hashCode(hash);
		}

		@Override
		public int hashCode() {
			return this.computed;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final HashKey other = (HashKey) obj;
			return Arrays.equals(this.value, other.value);
		}

		@Override
		public String toString() {
			return HexToString.byteToHex(this.value);
		}
	}

	private static final class Entry {
		long index;
		int size;

		public Entry(long index, int size) {
			this.index = index;
			this.size = size;
		}
	}

	private final PackedFile file = new PackedFile();
	private final Map<HashKey, Entry> entries = new HashMap<>();
	private StructArchiveRootElement rootElement;
	private boolean dirty;

	public PackedArchiveFile() {
	}

	public PackedArchiveFile(Path path) throws ArchiveHashCollisionException, IOException {
		open(path);
	}

	public void open(Path path) throws ArchiveHashCollisionException, IOException {
		this.file.open(path);

		if (this.file.getRootIndex() >= 0) {
			try (var reader = this.file.readEntry(this.file.getRootIndex())) {
				this.rootElement = new StructArchiveRootElement(reader);

				if (this.rootElement.version != VERSION) {
					throw new VersionMismatchException("Packed archive file", VERSION, this.rootElement.version);
				}

				if (this.rootElement.signature != SIGNATURE) {
					throw new SignatureMismatchException("Packed archive file", SIGNATURE, this.rootElement.signature);
				}
			}
		} else {
			this.rootElement = new StructArchiveRootElement(SIGNATURE, VERSION, 0, 0);
			this.file.setRootIndex(this.file.newEntry(StructArchiveRootElement.SIZE_IN_BYTES));
			this.dirty = true;
		}

		this.entries.clear();
		try (var reader = this.file.readEntry(this.rootElement.headerIdx)) {
			for (var i = 0; i < this.rootElement.entryCount; ++i) {
				final var index = reader.readUInt32();
				final var hashBuffer = new byte[20];
				reader.readInt8(hashBuffer, 0, hashBuffer.length);
				final var hash = new HashKey(hashBuffer);
				final var uncompressedSize = reader.readInt64();

				if (uncompressedSize > Integer.MAX_VALUE) {
					throw new IntegerOverflowException(); // TODO
				}

				final var oldEntry = this.entries.put(hash, new Entry(index, (int) uncompressedSize));
				if (oldEntry != null) {
					throw new ArchiveHashCollisionException(hash.toString()); // TODO
				}
			}
		}
	}

	public void validateFile() throws BinaryIOException, IOException {
		this.file.validateFile();
	}

	@Override
	public void close() throws IOException {
		try {
			if (this.dirty) {
				flush();
			}
			this.file.close();
		} finally {
			this.dirty = false;
			this.entries.clear();
			this.rootElement = null;
		}
	}

	public boolean isOpen() {
		return this.file.isOpen();
	}

	private void assertFileIsOpen() throws IOException {
		if (!isOpen()) {
			throw new FileClosedIOException();
		}
	}

	public void flush() throws IOException {
		assertFileIsOpen();

		if (this.rootElement.headerIdx <= 0) {
			final var capacity = (int) Math.max(1000, this.entries.size() * 1.50f) * StructArchiveEntry.SIZE_IN_BYTES;
			this.rootElement.headerIdx = (int) this.file.newEntry(capacity);
		} else {
			var capacity = this.file.entryCapacity(this.rootElement.headerIdx);
			if (capacity < this.entries.size() * StructArchiveEntry.SIZE_IN_BYTES) {
				this.file.releaseEntry(this.rootElement.headerIdx);
				capacity = (int) Math.max(1000, this.entries.size() * 1.50f) * StructArchiveEntry.SIZE_IN_BYTES;
				this.rootElement.headerIdx = (int) this.file.newEntry(capacity);
			}
		}

		try (var writer = this.file.writeEntry(this.rootElement.headerIdx)) {
			for (final var entry : this.entries.entrySet()) {
				writer.writeInt32(entry.getValue().index);
				writer.writeInt8(entry.getKey().value, 0, 20);
				writer.writeInt64(entry.getValue().size);
			}
		}

		this.rootElement.entryCount = this.entries.size();
		try (var writer = this.file.writeEntry(this.file.getRootIndex())) {
			this.rootElement.write(writer);
		}
		this.dirty = false;

		this.file.flush();
	}

	public void writeData(byte[] hash, byte[] data, boolean allowOverwrite) throws ArchiveHashCollisionException, IOException {
		assertFileIsOpen();

		final var hashKey = new HashKey(hash);
		final var length = data.length;
		var entry = this.entries.get(hashKey);
		if (entry == null) {
			entry = new Entry(this.file.newEntry(length), length);
			this.entries.put(hashKey, entry);
		} else {
			if (!allowOverwrite) {
				throw new ArchiveHashCollisionException(); // TODO
			}

			final var capacity = this.file.entryCapacity(entry.index);
			if (capacity < length) {
				this.file.releaseEntry(entry.index);
				entry.index = this.file.newEntry(length);
			}
			entry.size = length;
		}
		this.file.writeEntry(entry.index, data, 0, length);
		this.dirty = true;
	}

	public void replaceHash(byte[] oldHash, byte[] newHash) throws ArchiveHashCollisionException, ArchiveHashNotFoundException, IOException {
		assertFileIsOpen();

		final var oldKey = new HashKey(oldHash);
		final var newKey = new HashKey(newHash);
		if (this.entries.containsKey(newKey)) {
			throw new ArchiveHashCollisionException(); // TODO
		}
		final var entry = this.entries.remove(oldKey);
		if (entry == null) {
			throw new ArchiveHashNotFoundException(String.format("No entry found for hash %s", oldKey));
		}
		this.entries.put(newKey, entry);
		this.dirty = true;
	}

	public void deleteData(byte[] hash) throws ArchiveHashNotFoundException, IOException {
		assertFileIsOpen();

		final var key = new HashKey(hash);
		final var entry = this.entries.remove(key);
		if (entry == null) {
			throw new ArchiveHashNotFoundException(String.format("No entry found for hash %s", key));
		}
		this.file.releaseEntry(entry.index);
		this.dirty = true;
	}

	public byte[] getData(byte[] hash) throws ArchiveHashNotFoundException, IOException {
		assertFileIsOpen();

		final var key = new HashKey(hash);
		final var entry = this.entries.get(key);
		if (entry == null) {
			throw new ArchiveHashNotFoundException(String.format("No entry found for hash %s", key));
		}
		return this.file.readEntry(entry.index, null, 0, entry.size);
	}

	public boolean hasData(byte[] hash) throws IOException {
		assertFileIsOpen();
		return this.entries.containsKey(new HashKey(hash));
	}

	public int getNumberOfEntries() throws IOException {
		assertFileIsOpen();
		return this.entries.size();
	}

}
