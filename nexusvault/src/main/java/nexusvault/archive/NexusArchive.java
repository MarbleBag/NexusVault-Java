package nexusvault.archive;

import java.io.IOException;
import java.nio.file.Path;

import nexusvault.archive.impl.BaseNexusArchive;
import nexusvault.archive.util.IdxFileCollector;

public interface NexusArchive {

	/**
	 * Creates a default {@link NexusArchive}
	 */
	public static NexusArchive loadArchive(Path archiveOrIndex) throws IOException {
		return BaseNexusArchive.loadArchive(archiveOrIndex); // TODO
	}

	/**
	 * Contains informations about the source of a {@link NexusArchive}
	 */
	public static interface NexusArchiveSource {
		Path getIndexFile();

		Path getArchiveFile();
	}

	/**
	 * The root directory of an archive has no name and serves as an anchor for the highest elements an archive contains.
	 *
	 * @return root directory of this archive
	 * @see IdxPath
	 * @see IdxFileCollector
	 */
	IdxDirectory getRootDirectory();

	/**
	 * @throws NexusArchiveDisposedException
	 */
	NexusArchiveSource getSource();

	/**
	 * Instructs the archive to release all resources. Subsequent calls to this method should be safe. <br>
	 * <p>
	 * After a archive was disposed, it should not be used again.
	 */
	void dispose();

	/**
	 * @return <tt>true</tt> - if this archive was disposed. Calls to a disposed archive may cause undefined behavior
	 */
	boolean isDisposed();

}
