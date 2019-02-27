package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import kreed.io.util.BinaryReader;

public final class IndexFileReader {

	public IndexFile read(final Path indexPath) throws IOException {
		final BufferedFileAccessCache cache = new BufferedFileAccessCache(60000, indexPath, EnumSet.of(StandardOpenOption.READ), 16 * 1024,
				ByteOrder.LITTLE_ENDIAN);
		final AbstIndexFile indexFile = new CachedIndexFile(cache);
		indexFile.initialize();
		return indexFile;
	}

	public IndexFile read(final BinaryReader reader) throws IOException {
		final AbstIndexFile indexFile = new LoadedIndexFile(reader);
		indexFile.initialize();
		return indexFile;
	}

}
