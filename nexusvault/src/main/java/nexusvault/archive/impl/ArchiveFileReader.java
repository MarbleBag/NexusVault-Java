package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;

public final class ArchiveFileReader {

	public ArchiveFile read(final Path indexPath) throws IOException {
		final FileAccessCache cache = new FileAccessCache(60000, indexPath, 16 * 1024, ByteOrder.LITTLE_ENDIAN);
		final CachedArchiveFile archiveFile = new CachedArchiveFile(cache);
		archiveFile.initialize();
		return archiveFile;
	}

}