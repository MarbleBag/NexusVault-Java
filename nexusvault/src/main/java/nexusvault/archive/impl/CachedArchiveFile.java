package nexusvault.archive.impl;

import java.io.IOException;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;

final class CachedArchiveFile extends AbstArchiveFile {
	private final FileAccessCache cache;

	public CachedArchiveFile(FileAccessCache cache) {
		super();
		if (cache == null) {
			throw new IllegalArgumentException("'cache' must not be null");
		}
		this.cache = cache;
	}

	@Override
	public boolean isDisposed() {
		return cache.isShutDown();
	}

	@Override
	public void dispose() {
		cache.shutDown();
	}

	@Override
	protected BinaryReader getBinaryReader() throws IOException {
		return cache.getReader();
	}

	@Override
	public BinaryReader getArchiveData(byte[] hash) throws ArchiveEntryNotFoundException, IOException {
		final BinaryReader reader = super.getArchiveData(hash);
		cache.startExpiring();
		return reader;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("CachedArchiveFile [Source=");
		builder.append(cache.getSource());
		builder.append("]");
		return builder.toString();
	}

}