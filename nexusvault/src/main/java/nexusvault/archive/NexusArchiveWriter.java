package nexusvault.archive;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import kreed.io.util.BinaryReader;
import nexusvault.archive.impl.BaseNexusArchiveWriter;

public interface NexusArchiveWriter {

	public static NexusArchiveWriter createWriter() {
		return new BaseNexusArchiveWriter();
	}

	public static enum CompressionType {
		PREVIOUS(-1),
		NONE(0),
		ZIP(3),
		LZMA(5);
		private final int flag;

		private CompressionType(int flag) {
			this.flag = flag;
		}

		public int getFlag() {
			return flag;
		}
	}

	public static interface IntegrateableElementConfig {
		CompressionType getCompressionType();
	}

	public static interface IntegrateableElement {
		IdxPath getDestination();

		BinaryReader getData();

		void checkDataAvailability();

		IntegrateableElementConfig getConfig();
	}

	public static final class DefaultIntegrateableElementConfig implements IntegrateableElementConfig {
		private CompressionType compressionType = CompressionType.NONE;

		@Override
		public CompressionType getCompressionType() {
			return compressionType;
		}

		public void setCompressionType(CompressionType compressionType) {
			if (compressionType == null) {
				throw new IllegalArgumentException();
			}
			this.compressionType = compressionType;
		}

	}

	public static abstract class AbstIntegreateableElement implements IntegrateableElement {
		private final DefaultIntegrateableElementConfig integrateableElementConfig = new DefaultIntegrateableElementConfig();

		private final IdxPath destination;

		public AbstIntegreateableElement(IdxPath destination) {
			this.destination = destination;
		}

		@Override
		public DefaultIntegrateableElementConfig getConfig() {
			return integrateableElementConfig;
		}

		@Override
		public void checkDataAvailability() {
			// empty
		}

		@Override
		public IdxPath getDestination() {
			return destination;
		}

	}

	void load(Path archiveOrIndex) throws IOException;

	void dispose();

	boolean isDisposed();

	void write(Collection<? extends IntegrateableElement> src) throws IOException;

	void flush() throws IOException;

	// void delete(Collection<IdxPath> targets) throws IOException;

}
