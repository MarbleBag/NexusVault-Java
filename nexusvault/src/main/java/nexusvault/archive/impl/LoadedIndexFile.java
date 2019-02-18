package nexusvault.archive.impl;

import java.io.IOException;

import kreed.io.util.BinaryReader;
import nexusvault.archive.IdxException;

class LoadedIndexFile extends AbstIndexFile {

	private final BinaryReader reader;

	public LoadedIndexFile(BinaryReader reader) {
		super();
		if (reader == null) {
			throw new IllegalArgumentException("'reader' must not be null");
		}
		this.reader = reader;
	}

	@Override
	public boolean isDisposed() {
		return !reader.isOpen();
	}

	@Override
	public void dispose() {
		try {
			reader.close();
		} catch (final IOException e) {
			throw new IdxException("Dispose exception", e);
		}
	}

	@Override
	protected BinaryReader getBinaryReader() {
		return reader;
	}

}