package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import kreed.io.util.SeekableByteChannelBinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.NexusArchive;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.shared.exception.IntegerOverflowException;

public final class BaseNexusArchive implements NexusArchive {

	private static final class FileBasedNexusArchive {

		FileAccessCache idxFile;

		public FileBasedNexusArchive(Path idxPath, Path arcPath) {
			// TODO Auto-generated constructor stub
		}

	}

	public static BaseNexusArchive loadArchive(Path archiveOrIndex) throws IOException {

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

		// final FileBasedNexusArchive archive = new FileBasedNexusArchive(idxPath, arcPath);

		// TODO Auto-generated method stub
		return new BaseNexusArchive(idxPath, arcPath);
	}

	private final IndexFile indexFile;

	private BaseRootIdxDirectory rootDirectory;

	boolean isDisposed;

	private ArchiveFile fileArchive;

	private final FileAccessCache archiveCache;

	private BaseNexusArchive(Path idxPath, Path arcPath) throws IOException {
		final IndexFileReader indexFileReader = new IndexFileReader();
		this.indexFile = indexFileReader.read(idxPath);

		// TODO
		try (SeekableByteChannel fileStream = Files.newByteChannel(arcPath, EnumSet.of(StandardOpenOption.READ))) {
			final BinaryReader fileReader = new SeekableByteChannelBinaryReader(fileStream, ByteBuffer.allocate(32 * 1024).order(ByteOrder.LITTLE_ENDIAN));
			this.fileArchive = new ArchiveFileReader().read(fileReader);
		}
		this.archiveCache = new FileAccessCache(60000, arcPath, 4 * 1024 * 1024, ByteOrder.LITTLE_ENDIAN);

		initializeArchive();
	}

	private void initializeArchive() {
		rootDirectory = new BaseRootIdxDirectory(this.indexFile.getPackRootIdx());
		rootDirectory.setArchive(this);
	}

	@Override
	public BaseIdxDirectory getRootDirectory() {
		return rootDirectory;
	}

	@Override
	public NexusArchiveSource getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		if (isDisposed) {
			return;
		}

		isDisposed = true;
		this.indexFile.dispose();

		rootDirectory.setArchive(null);
		rootDirectory = null;
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	protected List<BaseIdxEntry> loadDirectory(BaseIdxDirectory baseIdxDirectory) {
		final IndexFile.IndexDirectoryData folderData = this.indexFile.getDirectoryData((int) baseIdxDirectory.getDirectoryPackIndex());

		final List<StructIdxDirectory> rawFolder = folderData.getDirectories();
		final List<StructIdxFile> rawFiles = folderData.getFileLinks();

		final List<BaseIdxEntry> childs = new ArrayList<>(rawFolder.size() + rawFiles.size());
		for (final StructIdxDirectory sdir : rawFolder) {
			final BaseIdxDirectory dir = new BaseIdxDirectory(baseIdxDirectory, sdir.name, sdir.directoryHeaderIdx);
			childs.add(dir);
		}

		for (final StructIdxFile sfile : rawFiles) {
			final BaseIdxFileLink file = new BaseIdxFileLink(baseIdxDirectory, sfile.name, sfile.flags, sfile.writeTime, sfile.uncompressedSize,
					sfile.compressedSize, sfile.hash, sfile.unk_034);
			childs.add(file);
		}

		return childs;
	}

	// protected ByteBuffer getData(BaseIdxFileLink baseIdxFileLink) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//

	void startArchiveAccess() { // TODO REWORK & DELETE

	}

	void endArchiveAccess() { // TODO REWORK & DELETE
		archiveCache.startExpiring();
	}

	BinaryReader getArchiveBinaryReader() throws IOException { // TODO REWORK & DELETE
		final BinaryReader reader = archiveCache.getReader();
		return reader;
	}

	PackHeader getPackHeaderForFile(IdxFileLink file) throws ArchiveEntryNotFoundException { // TODO REWORK & DELETE
		final AARCEntry entry = fileArchive.getEntry(file.getShaHash());
		final PackHeader header = fileArchive.getPackHeader(entry);
		return header;
	}

	// TODO REWORK & DELETE
	private final VaultUnpacker UNPACK_ZIP = new ZipInflaterVaultUnpacker();
	private final VaultUnpacker UNPACK_LZMA = new SevenZipLZMAVaultUnpacker();

	final private ByteBuffer getData(BaseIdxFileLink file) throws IOException, ArchiveEntryNotFoundException { // TODO REWORK & DELETE
		final PackHeader block = getPackHeaderForFile(file);

		startArchiveAccess();

		final BinaryReader reader = getArchiveBinaryReader();
		try {

			reader.seek(Seek.BEGIN, block.getOffset());
			final int compressionType = file.getFlags();

			ByteBuffer result = null;
			switch (compressionType) {
				case 3: // zip
					result = UNPACK_ZIP.unpack(reader, file.getCompressedSize(), file.getUncompressedSize());
					break;

				case 5: // lzma
					result = UNPACK_LZMA.unpack(reader, file.getCompressedSize(), file.getUncompressedSize());
					break;

				default: // uncompressed
					final long dataSize = block.getSize();
					if ((dataSize < 0) || (dataSize > Integer.MAX_VALUE)) {
						throw new IntegerOverflowException();
					}
					final byte[] buffer = new byte[(int) block.getSize()];
					reader.readInt8(buffer, 0, buffer.length);
					result = ByteBuffer.wrap(buffer);
			}

			result.order(ByteOrder.LITTLE_ENDIAN);
			return result;
		} finally {
			endArchiveAccess();
		}
	}

}
