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

/**
 * A {@link DataSource} which links to a file via {@link Path}. On request, the file will be read and returned. <br>
 * <b>Note:</b>The file will be read in little endian format.
 */
public final class FileDataSource extends AbstractDataSource implements DataSource {
	private final Path path;

	/**
	 * @param path
	 *            the location of the file which will be returned on request
	 * @throws IllegalArgumentException
	 *             if <code>path</code> is null
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