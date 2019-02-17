package nexusvault.archive.impl;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import nexusvault.archive.IdxPath;
import nexusvault.archive.NexusArchive;

public final class BaseNexusArchive implements NexusArchive {

	private static final class FileBasedNexusArchive {

		FileAccessCache idxFile;

		public FileBasedNexusArchive(Path idxPath, Path arcPath) {
			// TODO Auto-generated constructor stub
		}

	}

	public static BaseNexusArchive loadEmptyArchive() {
		// TODO Auto-generated method stub
		return null;
	}

	public static BaseNexusArchive loadArchive(Path archiveOrIndex) {

		String fileName = archiveOrIndex.getFileName().toString();
		Path idxPath = null;
		Path arcPath = null;

		if (fileName.endsWith(".index")) {
			idxPath = archiveOrIndex;
			fileName = fileName.substring(0, fileName.lastIndexOf(".index")) + ".archive";
			arcPath = archiveOrIndex.resolveSibling(fileName);
		} else if (fileName.endsWith(".archive")) {
			arcPath = archiveOrIndex;
			fileName = fileName.substring(0, fileName.lastIndexOf(".archive")) + ".index";
			idxPath = archiveOrIndex.resolveSibling(fileName);
		} else {
			throw new IllegalArgumentException(String.format("Path %s neither points to an index- nor an archive-file", archiveOrIndex));
		}

		if (!Files.exists(arcPath)) {
			throw new IllegalArgumentException(String.format("Archive-file at %s not found", arcPath));
		}

		if (!Files.exists(idxPath)) {
			throw new IllegalArgumentException(String.format("Index-file at %s not found", idxPath));
		}

		final FileBasedNexusArchive archive = new FileBasedNexusArchive(idxPath, arcPath);

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseIdxDirectory getRootDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseIdxFileLink setData(IdxPath path, ByteBuffer data, int flags) {
		final BaseIdxDirectory root = getRootDirectory();

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NexusArchiveSource getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	protected BaseIdxDirectory createDirectory(BaseIdxDirectory parent, String directoryName) {
		// TODO Auto-generated method stub
		return null;
	}

	protected BaseIdxFileLink createFileLink(BaseIdxDirectory parent, String fileLinkName, ByteBuffer data, int flags) {
		// TODO Auto-generated method stub
		return null;
	}

	protected List<BaseIdxEntry> loadDirectory(BaseIdxDirectory baseIdxDirectory) {
		// TODO Auto-generated method stub
		return null;
	}

	protected ByteBuffer getData(BaseIdxFileLink baseIdxFileLink) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void setData(BaseIdxFileLink baseIdxFileLink) {
		// TODO Auto-generated method stub
	}

}
