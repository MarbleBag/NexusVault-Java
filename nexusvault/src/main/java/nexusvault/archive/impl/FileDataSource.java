package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import kreed.io.util.BinaryReader;
import kreed.io.util.SeekableByteChannelBinaryReader;
import nexusvault.archive.NexusArchiveWriter.DataSource;

public final class FileDataSource extends AbstractDataSource implements DataSource {
	private final Path path;

	/**
	 * @throws IllegalArgumentException
	 *             if <tt>path</tt> is null
	 */
	public FileDataSource(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("'path' must not be null");
		}
		this.path = path;
	}

	@Override
	public BinaryReader getData() throws IOException {
		final var channel = Files.newByteChannel(path, StandardOpenOption.READ);
		final var buffer = ByteBuffer.allocate(1 << 16).order(ByteOrder.LITTLE_ENDIAN);
		return new SeekableByteChannelBinaryReader(channel, buffer);
	}
}