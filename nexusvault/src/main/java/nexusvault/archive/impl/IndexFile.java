package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;

public interface IndexFile {

	public static final class IndexDirectoryData {
		private final List<StructIdxDirectory> directories;
		private final List<StructIdxFile> fileLinks;

		public IndexDirectoryData(List<StructIdxDirectory> directories, List<StructIdxFile> fileLinks) {
			if (directories == null) {
				throw new IllegalArgumentException("'directories' must not be null");
			}
			if (fileLinks == null) {
				throw new IllegalArgumentException("'fileLinks' must not be null");
			}

			this.directories = directories;
			this.fileLinks = fileLinks;
		}

		public List<StructIdxDirectory> getDirectories() {
			return directories;
		}

		public List<StructIdxFile> getFileLinks() {
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
	 * @return index that is now associated to the given directory data
	 * @throws IOException
	 * @see {@link #getDirectoryData(long)}
	 */
	long writeDirectoryData(IndexDirectoryData data) throws IOException;

	/**
	 * Overwrites the data at <tt>packIdx</tt> position, or writes new data if <tt>packIdx</tt> is equal to {@link #UNUSED_INDEX}
	 *
	 * @param index
	 *            that should be overwriten, in case <tt>index</tt> is equal to {@link #UNUSED_INDEX}, a new entry will be written
	 * @param data
	 * @return index that is now associated to the given directory data
	 * @throws IOException
	 */
	long writeDirectoryData(long index, IndexDirectoryData data) throws IOException;

	void overwriteFileAttribute(long packIdx, int fileIndex, byte[] hash, StructIdxFile file) throws IOException;

	void flushWrite() throws IOException;

}
