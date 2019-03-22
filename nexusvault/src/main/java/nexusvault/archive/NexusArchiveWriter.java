package nexusvault.archive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import nexusvault.archive.impl.BaseNexusArchiveWriter;
import nexusvault.archive.impl.ByteBufferDataSource;
import nexusvault.archive.impl.FileDataSource;

public interface NexusArchiveWriter {

	public static enum CompressionType {
		// PREVIOUS(-1),
		NONE(0),
		ZIP(2 | 1),
		LZMA(4 | 1);

		private final int flag;

		private CompressionType(int flag) {
			this.flag = flag;
		}

		public int getFlag() {
			return flag;
		}
	}

	public static interface DataSource {

		public static DataSource wrap(ByteBuffer buffer) {
			return new ByteBufferDataSource(buffer);
		}

		public static DataSource wrap(Path file) {
			return new FileDataSource(file);
		}

		DataSourceConfig getConfig();

		BinaryReader getData() throws IOException;
	}

	public static final class DataSourceConfig {
		private CompressionType compressionType = CompressionType.NONE;

		public CompressionType getRequestedCompressionType() {
			return compressionType;
		}

		public void setRequestedCompressionType(CompressionType compressionType) {
			if (compressionType == null) {
				throw new IllegalArgumentException();
			}
			this.compressionType = compressionType;
		}
	}

	public static NexusArchiveWriter createWriter() {
		return new BaseNexusArchiveWriter();
	}

	void dispose();

	void flush() throws IOException;

	boolean isDisposed();

	void load(Path archiveOrIndex) throws IOException;

	void write(DataSource source, IdxPath destination) throws IOException;

	// void delete(Collection<IdxPath> targets) throws IOException;

}
