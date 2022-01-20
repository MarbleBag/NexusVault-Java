package nexusvault.vault.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;

import kreed.io.util.BinaryIOException;
import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryReaderDelegate;
import kreed.io.util.BinaryWriter;
import kreed.io.util.BinaryWriterDelegate;
import kreed.io.util.SeekableByteChannelBinaryReader;
import kreed.io.util.SeekableByteChannelBinaryWriter;

public final class BufferedFileAccess {

	private static final int SIZE_2_MB = 2 << 20;

	private static ByteBuffer createByteBuffer() {
		return ByteBuffer.allocate(SIZE_2_MB).order(ByteOrder.LITTLE_ENDIAN);
	}

	private FileAccessCache fileCache;
	private Path path;

	private ByteBuffer readBuffer;
	private BinaryReader reader;

	private ByteBuffer writeBuffer;
	private BinaryWriter writer;

	public BufferedFileAccess() {
	}

	public boolean isOpen() {
		return getPath() != null;
	}

	public void open(Path path, EnumSet<StandardOpenOption> openOptions) throws IOException {
		if (this.fileCache != null) {
			if (this.fileCache.getSource().equals(path)) {
				return;
			}
			close();
		}

		final var options = EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE);
		if (openOptions != null) {
			options.addAll(openOptions);
		}

		this.path = path;
		this.fileCache = new FileAccessCache(60000, path, options);
	}

	public void close() throws IOException {
		if (this.fileCache == null) {
			return;
		}

		this.path = null;

		final var exceptions = new ArrayList<Throwable>();

		if (this.reader != null) {
			try {
				this.reader.close();
				this.reader = null;
				this.readBuffer = null;
			} catch (final Throwable e) { // ignore
				exceptions.add(e);
			}
		}
		if (this.writer != null) {
			try {
				if (this.writer.isOpen()) {
					this.writer.flush();
				}
			} catch (final Throwable e) {
				exceptions.add(e);
			}
			try {
				this.writer.close();
				this.writer = null;
				this.writeBuffer = null;
			} catch (final Throwable e) { // ignore
				exceptions.add(e);
			}
		}

		try {
			this.fileCache.shutDown();
		} catch (final Throwable e) {
			exceptions.add(e);
		} finally {
			this.fileCache = null;

		}

		if (!exceptions.isEmpty()) {
			final var exception = new IOException();
			for (final var suppressed : exceptions) {
				exception.addSuppressed(suppressed);
			}
			throw exception;
		}
	}

	public Path getPath() {
		return this.path;
	}

	public BinaryReader getFileReader() throws IOException, BinaryIOException {
		if (this.fileCache == null) {
			throw new IllegalStateException("No file open");
		}

		if (this.readBuffer == null) {
			this.readBuffer = createByteBuffer();
		}

		if (this.reader == null || !this.reader.isOpen()) {
			// for some reason it is really really expensive to build those
			this.reader = new SeekableByteChannelBinaryReader(this.fileCache.getFileAccess(), this.readBuffer, false);
		}

		final BinaryReader delegate = new BinaryReaderDelegate(this.reader) {
			@Override
			public void close() throws IOException {
				if (isOpen()) {
					BufferedFileAccess.this.fileCache.startExpiring();
				}
				super.close();
			}
		};
		return delegate;
	}

	public BinaryWriter getFileWriter() throws IOException, BinaryIOException {
		if (this.fileCache == null) {
			throw new IllegalStateException("No file open");
		}

		if (this.writeBuffer == null) {
			this.writeBuffer = createByteBuffer();
		}

		this.fileCache.getFileAccess();
		if (this.writer == null || !this.writer.isOpen()) {
			// for some reason it is really really expensive to build those
			this.writer = new SeekableByteChannelBinaryWriter(this.fileCache.getFileAccess(), this.writeBuffer, false);
		}

		final BinaryWriter delegate = new BinaryWriterDelegate(this.writer) {
			@Override
			public void close() throws IOException {
				if (isOpen()) {
					BufferedFileAccess.this.fileCache.startExpiring();
				}
				super.close();
			}
		};
		return delegate;
	}

}
