package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;

@Deprecated
final class CachedArchiveFile extends AbstArchiveFile {

	private final BufferedFileAccessCache cache;

	public CachedArchiveFile(BufferedFileAccessCache cache) {
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
		return cache.getFileReader();
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

	@Override
	public void setTarget(Path path) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasArchiveData(byte[] hash) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumberOfEntries() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void prepareWriteMode() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNumberOfExpectedEntries(int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setArchiveData(byte[] hash, BinaryReader data) throws IOException {
		// TODO Auto-generated method stub

	}

}