package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public final class ArchiveFileReader {

	public ArchiveFile read(final Path indexPath) throws IOException {
		final BufferedFileAccessCache cache = new BufferedFileAccessCache(60000, indexPath, EnumSet.of(StandardOpenOption.READ), 16 * 1024,
				ByteOrder.LITTLE_ENDIAN);
		final CachedArchiveFile archiveFile = new CachedArchiveFile(cache);
		archiveFile.initialize();
		return archiveFile;
	}

}