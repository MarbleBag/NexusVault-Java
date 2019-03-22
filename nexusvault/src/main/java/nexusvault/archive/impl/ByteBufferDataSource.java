package nexusvault.archive.impl;

import java.nio.ByteBuffer;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.archive.NexusArchiveWriter.DataSource;

public final class ByteBufferDataSource extends AbstractDataSource implements DataSource {

	private final ByteBuffer buffer;

	/**
	 * @throws IllegalArgumentException
	 *             if <tt>buffer</tt> is null
	 */
	public ByteBufferDataSource(ByteBuffer buffer) {
		if (buffer == null) {
			throw new IllegalArgumentException("'buffer' must not be null");
		}
		this.buffer = buffer;
	}

	@Override
	public BinaryReader getData() {
		return new ByteBufferBinaryReader(buffer);
	}
}