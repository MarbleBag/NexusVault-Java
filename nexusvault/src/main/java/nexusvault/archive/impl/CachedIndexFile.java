package nexusvault.archive.impl;

import java.io.IOException;

import kreed.io.util.BinaryReader;
import nexusvault.archive.IdxException;

final class CachedIndexFile extends AbstIndexFile {
	private final FileAccessCache cache;

	public CachedIndexFile(FileAccessCache cache) {
		super();
		if (cache == null) {
			throw new IllegalArgumentException("'cache' must not be null");
		}
		this.cache = cache;
	}

	@Override
	public IndexDirectoryData getDirectoryData(int packIdx) {
		final IndexDirectoryData data = super.getDirectoryData(packIdx);
		cache.startExpiring();
		return data;
	}

	@Override
	protected BinaryReader getBinaryReader() {
		try {
			return cache.getReader();
		} catch (final IOException e) {
			throw new IdxException("Unable to read index-file", e);
		}
	}

	@Override
	public boolean isDisposed() {
		return this.cache.isShutDown();
	}

	@Override
	public void dispose() {
		this.cache.shutDown();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("CachedIndexFile [Source=");
		builder.append(cache.getSource());
		builder.append("]");
		return builder.toString();
	}

}