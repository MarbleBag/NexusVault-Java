package nexusvault.archive.impl;

import java.nio.file.Path;

public class ArchivePathLocator {
	public static Path getArchivePath(Path archiveOrIndex) {
		String fileName = archiveOrIndex.getFileName().toString();
		if (fileName.endsWith(".index")) {
			fileName = fileName.substring(0, fileName.lastIndexOf(".index")) + ".archive";
			return archiveOrIndex.resolveSibling(fileName);
		} else if (fileName.endsWith(".archive")) {
			return archiveOrIndex;
		} else {
			throw new IllegalArgumentException(String.format("Path %s neither points to an index- nor an archive-file", archiveOrIndex));
		}
	}

	public static Path getIndexPath(Path archiveOrIndex) {
		String fileName = archiveOrIndex.getFileName().toString();
		if (fileName.endsWith(".index")) {
			return archiveOrIndex;
		} else if (fileName.endsWith(".archive")) {
			fileName = fileName.substring(0, fileName.lastIndexOf(".archive")) + ".index";
			return archiveOrIndex.resolveSibling(fileName);
		} else {
			throw new IllegalArgumentException(String.format("Path %s neither points to an index- nor an archive-file", archiveOrIndex));
		}
	}
}