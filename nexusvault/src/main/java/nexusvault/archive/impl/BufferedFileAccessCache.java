package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.io.util.SeekableByteChannelBinaryReader;
import kreed.io.util.SeekableByteChannelBinaryWriter;

public final class BufferedFileAccessCache {

	private final FileAccessCache cache;
	private final int bufferSize;
	private final ByteOrder byteOrder;

	private BinaryReader reader;
	private BinaryWriter writer;

	private final Object lock = new Object();

	public BufferedFileAccessCache(long cacheTime, Path filePath, EnumSet<StandardOpenOption> fileAccessOption, int bufferSize, ByteOrder byteOrder) {
		if (bufferSize < Long.BYTES) {
			throw new IllegalArgumentException("Buffersize too small");
		}

		this.bufferSize = bufferSize;
		this.byteOrder = byteOrder;
		this.cache = new FileAccessCache(cacheTime, filePath, fileAccessOption);
	}

	public void shutDown() {
		cache.shutDown();
		reader = null;
		writer = null;
	}

	public boolean isShutDown() {
		return cache.isShutDown();
	}

	public BinaryReader getFileReader() throws IOException {
		if ((reader == null) || !reader.isOpen()) {
			synchronized (lock) {
				if ((reader == null) || !reader.isOpen()) {
					final ByteBuffer fileBuffer = ByteBuffer.allocateDirect(bufferSize).order(byteOrder);
					this.reader = new SeekableByteChannelBinaryReader(cache.getFileAccess(), fileBuffer);
				}
			}
		}
		return reader;
	}

	public BinaryWriter getFileWriter() throws IOException {
		if ((writer == null) || !writer.isOpen()) {
			synchronized (lock) {
				if ((writer == null) || !writer.isOpen()) {
					final ByteBuffer fileBuffer = ByteBuffer.allocateDirect(bufferSize).order(byteOrder);
					this.writer = new SeekableByteChannelBinaryWriter(cache.getFileAccess(), fileBuffer);
				}
			}
		}
		return writer;
	}

	public SeekableByteChannel getFileAccess() throws IOException {
		return cache.getFileAccess();
	}

	public Path getSource() {
		return cache.getSource();
	}

	public void startExpiring() {
		cache.startExpiring();
	}

}