package nexusvault.archive.impl;

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
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxFileLink;

@Deprecated
public final class BaseVaultReader extends AbstVaultReader {

	private Index2File fileIndex;
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
			throw new IllegalArgumentException(String.format("Path %s neither points to an index- nor an archive-file", path));
		}

		this.pathArchive = arcPath;

		if (!Files.exists(arcPath)) {
			throw new IllegalArgumentException(String.format("Archive-file at %s not found", arcPath));
		}

		if (!Files.exists(idxPath)) {
			throw new IllegalArgumentException(String.format("Index-file at %s not found", idxPath));
		}

		try (SeekableByteChannel fileStream = Files.newByteChannel(idxPath, EnumSet.of(StandardOpenOption.READ))) {
			final BinaryReader fileReader = new SeekableByteChannelBinaryReader(fileStream, ByteBuffer.allocate(32 * 1024).order(ByteOrder.LITTLE_ENDIAN));
			this.fileIndex = new IndexFileReader2().read(fileReader);
		}

		try (SeekableByteChannel fileStream = Files.newByteChannel(arcPath, EnumSet.of(StandardOpenOption.READ))) {
			final BinaryReader fileReader = new SeekableByteChannelBinaryReader(fileStream, ByteBuffer.allocate(32 * 1024).order(ByteOrder.LITTLE_ENDIAN));
			this.fileArchive = new ArchiveFileReader().read(fileReader);
		}

		this.archiveCache = new FileAccessCache(60000, this.pathArchive, 4 * 1024 * 1024, ByteOrder.LITTLE_ENDIAN);

		// TODO
		System.out.println("ARCHIVE: " + path);
		System.out.println(this.fileIndex.header.unknown_210);
		System.out.println(this.fileIndex.header.packHeaderCount);
		System.out.println(this.fileIndex.header.unk_224);
		System.out.println(this.fileIndex.aidx.unknown1);
		System.out.println(this.fileIndex.rootDirectory.countSubTree());
		System.out.println();
		System.out.println(this.fileArchive.header.unknown_210);
		System.out.println(this.fileArchive.header.headerCount);
		System.out.println(this.fileArchive.header.unk_224);
		System.out.println(this.fileArchive.aarc.entryCount);
		System.out.println(this.fileArchive.entries.size());
		System.out.println();
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
		final BinaryReader reader = archiveCache.getReader();
		return reader;
	}

	@Override
	PackHeader getPackHeaderForFile(IdxFileLink file) throws ArchiveEntryNotFoundException {
		final AARCEntry entry = fileArchive.getEntry(file.getShaHash());
		final PackHeader header = fileArchive.getPackHeader(entry);
		return header;
	}

}
