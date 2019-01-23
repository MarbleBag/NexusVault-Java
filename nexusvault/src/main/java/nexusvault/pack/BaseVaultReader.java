package nexusvault.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import kreed.io.util.BinaryReader;
import kreed.io.util.SeekableByteChannelBinaryReader;
import nexusvault.pack.archive.AARCEntry;
import nexusvault.pack.archive.ArchiveFile;
import nexusvault.pack.archive.ArchiveFileReader;
import nexusvault.pack.index.IdxDirectory;
import nexusvault.pack.index.IdxFileLink;
import nexusvault.pack.index.IndexFile;
import nexusvault.pack.index.IndexFileReader;

final class BaseVaultReader extends AbstVaultReader {

	private IndexFile fileIndex;
	private ArchiveFile fileArchive;
	private Path pathArchive;
	private FileAccessCache archiveCache;

	public BaseVaultReader() {

	}

	@Override
	public void readArchive(Path path) throws IOException {
		dispose();

		String fileName = path.getFileName().toString();
		Path idxPath = null;
		Path arcPath = null;

		if (fileName.endsWith(".index")) {
			idxPath = path;
			fileName = fileName.substring(0, fileName.lastIndexOf(".index")) + ".archive";
			arcPath = path.resolveSibling(fileName);
		} else if (fileName.endsWith(".archive")) {
			arcPath = path;
			fileName = fileName.substring(0, fileName.lastIndexOf(".archive")) + ".index";
			idxPath = path.resolveSibling(fileName);
		} else {
			throw new IllegalArgumentException("todo");
		}

		this.pathArchive = arcPath;

		try (SeekableByteChannel fileStream = Files.newByteChannel(idxPath, EnumSet.of(StandardOpenOption.READ))) {
			final BinaryReader fileReader = new SeekableByteChannelBinaryReader(fileStream, ByteBuffer.allocate(32 * 1024).order(ByteOrder.LITTLE_ENDIAN));
			this.fileIndex = new IndexFileReader().read(fileReader);
		}

		try (SeekableByteChannel fileStream = Files.newByteChannel(arcPath, EnumSet.of(StandardOpenOption.READ))) {
			final BinaryReader fileReader = new SeekableByteChannelBinaryReader(fileStream, ByteBuffer.allocate(32 * 1024).order(ByteOrder.LITTLE_ENDIAN));
			this.fileArchive = new ArchiveFileReader().read(fileReader);
		}

		this.archiveCache = new FileAccessCache(60000, this.pathArchive, 4 * 1024 * 1024, ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void dispose() {
		if (this.archiveCache == null) {
			return;
		}

		this.fileIndex = null;
		this.fileArchive = null;
		this.pathArchive = null;

		this.archiveCache.shutDown();
		this.archiveCache = null;
	}

	@Override
	public boolean isDisposed() {
		return this.archiveCache == null;
	}

	@Override
	public IdxDirectory getRootFolder() {
		return fileIndex.getRootDirectory();
	}

	@Override
	void startArchiveAccess() {

	}

	@Override
	void endArchiveAccess() {
		archiveCache.startExpiring();
	}

	@Override
	BinaryReader getArchiveBinaryReader() throws IOException {
		final BinaryReader reader = archiveCache.getChannel();
		return reader;
	}

	@Override
	PackHeader getPackHeaderForFile(IdxFileLink file) {
		final AARCEntry entry = fileArchive.getEntry(file.getShaHash());
		final PackHeader header = fileArchive.getPackHeader(entry);
		return header;
	}

}
