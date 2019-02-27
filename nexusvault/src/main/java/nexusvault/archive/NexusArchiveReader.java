package nexusvault.archive;

import java.io.IOException;
import java.nio.file.Path;

import nexusvault.archive.impl.BaseNexusArchiveReader;
import nexusvault.archive.util.IdxFileCollector;

public interface NexusArchiveReader {

	/**
	 * Creates a default {@link NexusArchiveReader}
	 */
	public static NexusArchiveReader loadArchive(Path archiveOrIndex) throws IOException {
		return BaseNexusArchiveReader.loadArchive(archiveOrIndex); // TODO
	}

	/**
	 * Contains informations about the source of a {@link NexusArchiveReader}
	 */
	public static interface NexusArchiveSource {
		Path getIndexFile();

		Path getArchiveFile();
	}

	void load(Path archiveOrIndex) throws IOException;

	void reload() throws IOException;

	/**
	 * The root directory of an archive has no name and serves as an anchor for the highest elements an archive contains.
	 *
	 * @return root directory of this archive
	 * @see IdxPath
	 * @see IdxFileCollector
	 */
	IdxDirectory getRootDirectory();

	/**
	 * Returns the current source of this NexusArchive. <br>
	 * This information will still be retrievable after this archive was {@link #dispose() disposed} and will be equal to the value of this method before the
	 * archive was disposed.
	 *
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
