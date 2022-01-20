package nexusvault.vault;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import kreed.io.util.BinaryIOException;
import nexusvault.vault.IdxEntry.IdxDirectory;
import nexusvault.vault.util.IdxFileCollector;

public interface NexusArchive {

	/**
	 * Contains informations about the source of a {@link NexusArchive}
	 */
	public static final class NexusArchiveFiles {
		private final Path indexFile;
		private final Path archiveFile;

		public NexusArchiveFiles(Path indexFile, Path archiveFile) {
			this.indexFile = indexFile;
			this.archiveFile = archiveFile;
		}

		public Path getIndexFile() {
			return this.indexFile;
		}

		public Path getArchiveFile() {
			return this.archiveFile;
		}
	}

	public static enum CompressionType {
		UNCOMPRESSED(0),
		LZMA(5),
		ZIP(3);
		public final int flag;

		private CompressionType(int flag) {
			this.flag = flag;
		}
	}

	/**
	 * Creates a {@link NexusArchive} which is linked to the given file.If an archive file is given, the archive will look for an index file with the same name
	 * and vice versa. If those files do not exist they will be created.
	 * <p>
	 * This function is equivalent to {@link #load(Path)}
	 *
	 * @param archiveOrIndex
	 *            the location of an archive or index file
	 * @return a archive which is build from the given source
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static NexusArchive open(Path archiveOrIndex) throws IOException {
		return new NexusArchiveImpl(archiveOrIndex);
	}

	public static NexusArchive create() {
		return new NexusArchiveImpl();
	}

	void load(Path archiveOrIndex) throws IOException;

	void reload() throws IOException;

	/**
	 * Instructs the archive to release all resources. Subsequent calls to this method should be safe. <br>
	 * <p>
	 * After an archive was disposed, subsequent calls to other methods may throw an error.
	 */
	void dispose();

	/**
	 * @return <code>true</code> - if this archive was disposed. Calls to a disposed archive will have undefined behavior
	 */
	boolean isDisposed();

	/**
	 * Returns the current files of this {@link NexusArchive} <br>
	 * This information will still be retrievable after this archive was {@link #dispose() disposed} and will be equal to the value of this method before the
	 * archive was disposed.
	 *
	 * @return the files of this archive
	 */
	NexusArchiveFiles getFiles();

	/**
	 * The root directory of an archive has no name and serves as an anchor for the highest elements an archive contains.
	 *
	 * @return root directory of this archive
	 * @see IdxPath
	 * @see IdxFileCollector
	 * @throws VaultDisposedException
	 *             if the archive was disposed
	 */
	IdxDirectory getRootDirectory();

	int getNumberOfFiles();

	void write(IdxPath path, byte[] data, CompressionType compression) throws IOException;

	void delete(IdxPath path) throws IOException;

	void move(IdxPath from, IdxPath to) throws IOException;

	Optional<IdxEntry> find(IdxPath path);

	void validateArchive() throws BinaryIOException, IOException;

}
