package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.ArchiveException;
import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxException;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.NexusArchive;
import nexusvault.archive.NexusArchiveDisposedException;
import nexusvault.shared.exception.IntegerOverflowException;

public final class BaseNexusArchiveReader implements NexusArchive {

	private static final class BasePathNexusArchiveSource implements NexusArchiveSource { // TODO
		private final Path indexFile;
		private final Path archiveFile;

		public BasePathNexusArchiveSource(Path indexFile, Path archiveFile) {
			this.indexFile = indexFile;
			this.archiveFile = archiveFile;
		}

		@Override
		public Path getIndexFile() {
			return this.indexFile;
		}

		@Override
		public Path getArchiveFile() {
			return this.archiveFile;
		}
	}

	private static final LZMACodec CODEC_LZMA = new LZMACodec();
	private static final ZipCodec CODEC_ZIP = new ZipCodec();

	public static BaseNexusArchiveReader buildArchive() {
		return new BaseNexusArchiveReader();
	}

	public static BaseNexusArchiveReader loadArchive(Path archiveOrIndex) throws IOException {
		return new BaseNexusArchiveReader(archiveOrIndex);
	}

	private final Object synchronizationLock = new Object();

	private IndexFile indexFile;
	private ArchiveFile archiveFile;
	private NexusArchiveSource source;

	private BaseRootIdxDirectory rootDirectory;

	boolean isDisposed = true;

	private BaseNexusArchiveReader(Path archiveOrIndex) throws IOException {
		super();
		load(archiveOrIndex);
	}

	private BaseNexusArchiveReader() {
		super();
	}

	@Override
	public void reload() throws IOException {
		final NexusArchiveSource source = getSource();
		load(source.getIndexFile(), source.getArchiveFile());
	}

	@Override
	public void load(Path archiveOrIndex) throws IOException {
		final Path idxPath = ArchivePathLocator.getIndexPath(archiveOrIndex);
		final Path arcPath = ArchivePathLocator.getArchivePath(archiveOrIndex);
		load(idxPath, arcPath);
	}

	private void load(Path idxPath, Path arcPath) throws IOException {
		if (!Files.exists(idxPath)) {
			throw new IllegalArgumentException(String.format("Index-file at %s not found", idxPath));
		}
		if (!Files.exists(arcPath)) {
			throw new IllegalArgumentException(String.format("Archive-file at %s not found", arcPath));
		}

		synchronized (this.synchronizationLock) {
			dispose();

			this.indexFile = IndexFile.createIndexFile();
			this.indexFile.openFile(idxPath);

			this.archiveFile = ArchiveFile.createArchiveFile();
			this.archiveFile.openFile(arcPath);

			this.source = new BasePathNexusArchiveSource(idxPath, arcPath);

			initializeArchive();
			this.isDisposed = false;
		}
	}

	private void initializeArchive() {
		this.rootDirectory = new BaseLazyRootIdxDirectory(this.indexFile.getRootDirectoryIndex());
		this.rootDirectory.setArchive(this);
	}

	@Override
	public IdxDirectory getRootDirectory() {
		if (isDisposed()) {
			throw new NexusArchiveDisposedException();
		}
		return this.rootDirectory;
	}

	@Override
	public NexusArchiveSource getSource() {
		return this.source;
	}

	@Override
	public void dispose() {
		if (this.isDisposed) {
			return;
		}

		synchronized (this.synchronizationLock) {
			if (this.isDisposed) {
				return;
			}

			this.isDisposed = true;

			try {
				this.indexFile.closeFile();
			} catch (final IOException e) { // ignore
			}
			try {
				this.archiveFile.closeFile();
			} catch (final IOException e) { // ignore
			}

			this.indexFile = null;
			this.archiveFile = null;

			this.rootDirectory = null;
		}
	}

	@Override
	public boolean isDisposed() {
		return this.isDisposed;
	}

	protected List<BaseIdxEntry> loadDirectory(BaseIdxDirectory baseIdxDirectory) {
		IndexFile.IndexDirectoryData folderData;

		synchronized (this.synchronizationLock) {
			if (isDisposed()) {
				throw new NexusArchiveDisposedException();
			}

			try {
				folderData = this.indexFile.getDirectoryData((int) baseIdxDirectory.getDirectoryPackIndex());
			} catch (final IOException e) {
				throw new IdxException("Unable to read index-file", e);
			}
		}

		final List<IdxDirectoryAttribute> folderAttributes = folderData.getDirectories();
		final List<IdxFileAttribute> fileAttributes = folderData.getFileLinks();

		final List<BaseIdxEntry> childs = new ArrayList<>(folderAttributes.size() + fileAttributes.size());
		for (final IdxDirectoryAttribute folderAttribute : folderAttributes) {
			final BaseIdxDirectory dir = new BaseLazyIdxDirectory(baseIdxDirectory, folderAttribute.getName(), folderAttribute.getDirectoryIndex());
			childs.add(dir);
		}

		for (final IdxFileAttribute sfile : fileAttributes) {
			final BaseIdxFileLink fileAttribute = new BaseIdxFileLink(baseIdxDirectory, sfile.getName(), sfile.getFlags(), sfile.getWriteTime(),
					sfile.getUncompressedSize(), sfile.getCompressedSize(), sfile.getHash(), sfile.getUnk_034());
			childs.add(fileAttribute);
		}

		return childs;
	}

	protected ByteBuffer getData(IdxFileLink file) throws IOException, ArchiveEntryNotFoundException {

		BinaryReader reader;
		synchronized (this.synchronizationLock) {
			if (isDisposed()) {
				throw new NexusArchiveDisposedException();
			}

			reader = getDataFromArchive(file);
		}

		final int compressionType = file.getFlags();

		ByteBuffer result = null;
		switch (compressionType) {
			case 3:
				result = CODEC_ZIP.decode(reader, file.getCompressedSize(), file.getUncompressedSize());
				break;

			case 5:
				result = CODEC_LZMA.decode(reader, file.getCompressedSize(), file.getUncompressedSize());
				break;

			default: // uncompressed
				if (file.getUncompressedSize() < 0 || file.getUncompressedSize() > Integer.MAX_VALUE) {
					throw new IntegerOverflowException(); // TODO
				}
				if (reader.size() != file.getUncompressedSize()) {
					throw new ArchiveException(); // TODO
				}

				if (reader instanceof ByteBufferBinaryReader) {
					result = ((ByteBufferBinaryReader) reader).getSource();
				} else {
					final byte[] buffer = new byte[(int) reader.size()];
					reader.readInt8(buffer, 0, buffer.length);
					result = ByteBuffer.wrap(buffer);
				}
		}

		result.order(reader.getOrder());
		return result;
	}

	private BinaryReader getDataFromArchive(IdxFileLink file) throws IOException {
		try {
			return this.archiveFile.getArchiveData(file.getHash());
		} catch (final ArchiveEntryNotFoundException e) {
			throw new ArchiveEntryNotFoundException("Entry from index file can not be located in archive file. Archive or index file might be corrupted", e);
		}
	}

}
