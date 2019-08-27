package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IndexFile {

	public static final class IndexDirectoryData {
		private final List<IdxDirectoryAttribute> directories;
		private final List<IdxFileAttribute> fileLinks;

		public IndexDirectoryData(List<IdxDirectoryAttribute> directories, List<IdxFileAttribute> fileLinks) {
			if (directories == null) {
				throw new IllegalArgumentException("'directories' must not be null");
			}
			if (fileLinks == null) {
				throw new IllegalArgumentException("'fileLinks' must not be null");
			}

			this.directories = directories;
			this.fileLinks = fileLinks;
		}

		public List<IdxDirectoryAttribute> getDirectories() {
			return directories;
		}

		public List<IdxFileAttribute> getFileLinks() {
			return fileLinks;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("IndexDirectoryData [#directories=");
			builder.append(directories.size());
			builder.append(", #fileLinks=");
			builder.append(fileLinks.size());
			builder.append("]");
			return builder.toString();
		}

	}

	public static final int UNUSED_INDEX = -1;

	public static IndexFile createIndexFile() {
		return new BaseIndexFile();
	}

	void openFile(Path path) throws IOException;

	void closeFile() throws IOException;

	Path getFile();

	boolean isFileOpen();

	int getDirectoryCount();

	int getRootDirectoryIndex();

	IndexDirectoryData getDirectoryData(long index) throws IOException;

	void enableWriteMode() throws IOException;

	void setEstimatedNumberForWriteEntries(int count) throws IOException;

	/**
	 * Writes new data to the index file.
	 *
	 * @param data
	 *            will be written to the file
	 * @return index that is now associated to the given directory data
	 * @throws IOException
	 *             if an I/O error occurs
	 * @see #getDirectoryData(long)
	 */
	long writeDirectoryData(IndexDirectoryData data) throws IOException;

	/**
	 * Overwrites the data at <code>packIdx</code> position, or writes new data if <code>packIdx</code> is equal to {@link #UNUSED_INDEX}
	 *
	 * @param index
	 *            that should be overwriten, in case <code>index</code> is equal to {@link #UNUSED_INDEX}, a new entry will be created
	 * @param data
	 *            will be written to the file
	 * @return index that is now associated to the given directory data
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	long writeDirectoryData(long index, IndexDirectoryData data) throws IOException;

	void overwriteFileAttribute(long packIdx, int fileIndex, byte[] hash, IdxFileAttribute fileAttribute) throws IOException;

	void flushWrite() throws IOException;

}
