package nexusvault.archive.impl;

import java.nio.ByteOrder;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;

public final class IndexFileReader {

	public IndexFile read(final Path indexPath) {
		final FileAccessCache cache = new FileAccessCache(60000, indexPath, 16 * 1024, ByteOrder.LITTLE_ENDIAN);
		final AbstIndexFile indexFile = new CachedIndexFile(cache);
		indexFile.initialize();
		return indexFile;
	}

	public IndexFile read(final BinaryReader reader) {
		final AbstIndexFile indexFile = new LoadedIndexFile(reader);
		indexFile.initialize();
		return indexFile;
	}

}
