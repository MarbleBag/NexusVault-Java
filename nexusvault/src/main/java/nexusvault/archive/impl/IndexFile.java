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
			this.directories = directories;
			this.fileLinks = fileLinks;
		}

		public List<StructIdxDirectory> getDirectories() {
			return directories;
		}

		public List<StructIdxFile> getFileLinks() {
			return fileLinks;
		}
	}

	public static IndexFile createIndexFile() {
		return new BaseIndexFile();
	}

	void setTarget(Path path) throws IOException;

	void dispose();

	boolean isDisposed();

	int getPackRootIdx();

	IndexDirectoryData getDirectoryData(int packIdx) throws IOException;

	void prepareWriteMode() throws IOException;

	void setNumberOfExpectedEntries(int count);

	void setDirectoryData(int packIdx, IndexDirectoryData data) throws IOException;

}
