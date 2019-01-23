package nexusvault.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import nexusvault.pack.index.IdxDirectory;
import nexusvault.pack.index.IdxFileLink;

public interface VaultReader {

	public static VaultReader createVaultReader() {
		return new BaseVaultReader();
	}

	void readArchive(Path path) throws IOException;

	ByteBuffer getData(IdxFileLink file) throws IOException;

	IdxDirectory getRootFolder();

	void dispose();

	boolean isDisposed();
}
