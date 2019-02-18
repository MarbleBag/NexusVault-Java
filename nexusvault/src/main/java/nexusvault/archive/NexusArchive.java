package nexusvault.archive;

import java.io.IOException;
import java.nio.file.Path;

import nexusvault.archive.impl.BaseNexusArchive;

public interface NexusArchive {

	public static NexusArchive loadArchive(Path archiveOrIndex) throws IOException {
		return BaseNexusArchive.loadArchive(archiveOrIndex); // TODO
	}

	public static interface NexusArchiveSource {

	}

	public static interface PathNexusArchiveSource extends NexusArchiveSource {

	}

	public static interface MemoryNexusArchiveSource extends NexusArchiveSource {

	}

	/**
	 *
	 * @return
	 * @throws NexusArchiveDisposedException
	 */
	IdxDirectory getRootDirectory();

	/**
	 *
	 * @return
	 * @throws NexusArchiveDisposedException
	 */
	NexusArchiveSource getSource();

	void dispose();

	boolean isDisposed();

}
