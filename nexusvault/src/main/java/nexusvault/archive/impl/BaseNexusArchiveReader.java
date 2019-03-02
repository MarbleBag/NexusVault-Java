package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import nexusvault.archive.NexusArchiveDisposedException;
import nexusvault.archive.NexusArchiveReader;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.shared.exception.IntegerOverflowException;

public class BaseNexusArchiveReader implements NexusArchiveReader {

	private static final class BasePathNexusArchiveSource implements NexusArchiveSource { // TODO
		private final Path indexFile;
		private final Path archiveFile;

		public BasePathNexusArchiveSource(Path indexFile, Path archiveFile) {
			this.indexFile = indexFile;
			this.archiveFile = archiveFile;
		}

		@Override
		public Path getIndexFile() {
			return indexFile;
		}

		@Override
		public Path getArchiveFile() {
			return archiveFile;
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

	private IndexFile indexFile;
	private ArchiveFile archiveFile;
	private NexusArchiveSource source;

	private BaseRootIdxDirectory rootDirectory;

	boolean isDisposed = true;

	protected BaseNexusArchiveReader(Path archiveOrIndex) throws IOException {
		load(archiveOrIndex);
	}

	protected BaseNexusArchiveReader() {

	}

	@Override
	public void reload() throws IOException {
		final NexusArchiveSource source = getSource();
		load(source.getIndexFile(), source.getArchiveFile());
	}

	@Override
	public void load(Path archiveOrIndex) throws IOException {
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

		load(idxPath, arcPath);
	}

	private void load(Path idxPath, Path arcPath) throws IOException {
		if (!Files.exists(idxPath)) {
			throw new IllegalArgumentException(String.format("Index-file at %s not found", idxPath));
		}
		if (!Files.exists(arcPath)) {
			throw new IllegalArgumentException(String.format("Archive-file at %s not found", arcPath));
		}

		dispose();

		final IndexFileReader indexFileReader = new IndexFileReader();
		this.indexFile = indexFileReader.read(idxPath);

		final ArchiveFileReader archiveFileReader = new ArchiveFileReader();
		this.archiveFile = archiveFileReader.read(arcPath);

		this.source = new BasePathNexusArchiveSource(idxPath, arcPath);

		initializeArchive();
		isDisposed = false;
	}

	private void initializeArchive() {
		this.rootDirectory = new BaseLazyRootIdxDirectory(this.indexFile.getPackRootIdx());
		this.rootDirectory.setArchive(this);
	}

	@Override
	public IdxDirectory getRootDirectory() {
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

		this.isDisposed = true;
		this.indexFile.dispose();
		this.archiveFile.dispose();

		this.indexFile = null;
		this.archiveFile = null;

		this.rootDirectory = null;
	}

	@Override
	public boolean isDisposed() {
		return this.isDisposed;
	}

	protected List<BaseIdxEntry> loadDirectory(BaseIdxDirectory baseIdxDirectory) {
		if (isDisposed()) {
			throw new NexusArchiveDisposedException();
		}

		IndexFile.IndexDirectoryData folderData;

		try {
			folderData = this.indexFile.getDirectoryData((int) baseIdxDirectory.getDirectoryPackIndex());
		} catch (final IOException e) {
			throw new IdxException("Unable to read index-file", e);
		}

		final List<StructIdxDirectory> rawFolder = folderData.getDirectories();
		final List<StructIdxFile> rawFiles = folderData.getFileLinks();

		final List<BaseIdxEntry> childs = new ArrayList<>(rawFolder.size() + rawFiles.size());
		for (final StructIdxDirectory sdir : rawFolder) {
			final BaseIdxDirectory dir = new BaseLazyIdxDirectory(baseIdxDirectory, sdir.name, sdir.directoryHeaderIdx);
			childs.add(dir);
		}

		for (final StructIdxFile sfile : rawFiles) {
			final BaseIdxFileLink file = new BaseIdxFileLink(baseIdxDirectory, sfile.name, sfile.flags, sfile.writeTime, sfile.uncompressedSize,
					sfile.compressedSize, sfile.hash, sfile.unk_034);
			childs.add(file);
		}

		return childs;
	}

	protected ByteBuffer getData(IdxFileLink file) throws IOException, ArchiveEntryNotFoundException {
		if (isDisposed()) {
			throw new NexusArchiveDisposedException();
		}

		final BinaryReader reader = this.archiveFile.getArchiveData(file.getShaHash());

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
				if ((file.getUncompressedSize() < 0) || (file.getUncompressedSize() > Integer.MAX_VALUE)) {
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

		result.order(ByteOrder.LITTLE_ENDIAN);
		return result;
	}

}