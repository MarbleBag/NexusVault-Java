package nexusvault.archive.impl;

import java.util.List;

import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.archive.struct.StructPackHeader;

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

	int getPackRootIdx();

	int getPackCount();

	StructPackHeader getPack(int packIdx);

	IndexFile.IndexDirectoryData getDirectoryData(int packIdx);

	boolean isDisposed();

	void dispose();
}