package nexusvault.archive;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import nexusvault.archive.impl.BaseNexusArchive;

public interface NexusArchive {

	public static NexusArchive loadEmptyArchive() {
		return BaseNexusArchive.loadEmptyArchive(); // TODO
	}

	public static NexusArchive loadArchive(Path archiveOrIndex) {
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
	 * @param path
	 * @param data
	 * @param flags
	 * @return
	 * @throws NexusArchiveDisposedException
	 */
	@Deprecated
	IdxFileLink setData(IdxPath path, ByteBuffer data, int flags);

	/**
	 *
	 * @return
	 * @throws NexusArchiveDisposedException
	 */
	NexusArchiveSource getSource();

	void dispose();

	boolean isDisposed();

}
