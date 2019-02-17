package nexusvault.archive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import nexusvault.archive.impl.BaseVaultReader;

public interface ArchiveReader {

	/**
	 * Creates a default {@link ArchiveReader}
	 */
	public static ArchiveReader createVaultReader() {
		return new BaseVaultReader();
	}

	/**
	 *
	 *
	 * @param path
	 *            - either to an index- or archive-file
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	void readArchive(Path path) throws IOException;

	/**
	 * @param file
	 * @return a buffer containing the data which are associated with <tt>file</tt>. In case the data was compressed, the returned buffer is already
	 *         uncompressed.
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ArchiveEntryNotFoundException
	 *             if the entry for <tt>file</tt> can not be found
	 */
	ByteBuffer getData(IdxFileLink file) throws IOException, ArchiveEntryNotFoundException;

	/**
	 * The root directory of an archive has no name and serves as an anchor for the highest elements an archive contains.
	 *
	 * @return root directory of this archive
	 */
	IdxDirectory getRootFolder();

	/**
	 * Instructs the reader to release all resources. Subsequent calls to this method should be safe. <br>
	 * A disposed reader can be used again after calling {@link #readArchive(Path)}
	 * <p>
	 * After a reader was disposed, it should not be used again until a new file is loaded.
	 */
	void dispose();

	/**
	 * @return <tt>true</tt> - if this reader was disposed. Calls to a disposed reader may cause undefined behavior
	 */
	boolean isDisposed();
}
