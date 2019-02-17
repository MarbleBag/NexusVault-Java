package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import nexusvault.archive.struct.StructAIDX;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructIdxDirectory;
import nexusvault.archive.struct.StructIdxEntry;
import nexusvault.archive.struct.StructIdxFile;
import nexusvault.archive.struct.StructPackHeader;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class IndexFileReader {

	public static interface IndexFile {
		int getPackRootIdx();

		int getPackCount();

		StructPackHeader getPack(int packIdx);

		List<StructIdxEntry> getDirectoryData(int packIdx);
	}

	private abstract class BaseIndexFile implements IndexFile {
		protected final StructReader<BinaryReader> structReader;

		public BaseIndexFile() {
			final DataReadDelegator<BinaryReader> readerDelegator = DataReadDelegator.build(new kreed.reflection.struct.reader.BinaryReader());
			structReader = StructReader.build(StructFactory.build(), readerDelegator, true);
		}

		@Override
		public List<StructIdxEntry> getDirectoryData(int packIdx) {
			final BinaryReader reader = getBinaryReader();
			final StructPackHeader pack = getPack(packIdx);

			reader.seek(Seek.BEGIN, pack.getOffset());

			final long numSubDirectories = reader.readUInt32();
			final long numFiles = reader.readUInt32();
			final String nameTwine = extractNames(reader, pack.getSize(), numSubDirectories, numFiles);

			final long numberOfChilds = numSubDirectories + numFiles;
			if ((numberOfChilds > Integer.MAX_VALUE) || (numberOfChilds < 0)) {
				throw new IntegerOverflowException("number of childs");
			}

			final List<StructIdxEntry> entries = new ArrayList<>((int) numberOfChilds);
			for (int i = 0; i < numSubDirectories; ++i) {
				final StructIdxDirectory dir = structReader.read(new StructIdxDirectory(), reader);
				final int nullTerminator = nameTwine.indexOf(0, (int) dir.nameOffset);
				dir.name = nameTwine.substring((int) dir.nameOffset, nullTerminator);
				entries.add(dir);
			}

			for (long i = 0; i < numFiles; ++i) {
				final StructIdxFile fileLink = structReader.read(new StructIdxFile(), reader);
				final int nullTerminator = nameTwine.indexOf(0, (int) fileLink.nameOffset);
				fileLink.name = nameTwine.substring((int) fileLink.nameOffset, nullTerminator);
				entries.add(fileLink);
			}

			return entries;
		}

		private String extractNames(BinaryReader reader, long blockSize, long numSubDirectories, long numFiles) {
			final long dataPosition = reader.getPosition();
			final long nameOffset = (numSubDirectories * StructIdxDirectory.SIZE_IN_BYTES) + (numFiles * StructIdxFile.SIZE_IN_BYTES);
			final long nameLengthInByte = blockSize - (2 * Integer.BYTES) - nameOffset;
			reader.seek(Seek.CURRENT, nameOffset);
			final byte[] entryNameAsBytes = new byte[(int) nameLengthInByte];
			reader.readInt8(entryNameAsBytes, 0, entryNameAsBytes.length);
			final String nameTwine = new String(entryNameAsBytes, Charset.forName("UTF8"));
			reader.seek(Seek.BEGIN, dataPosition);
			return nameTwine;
		}

		@Override
		abstract public StructPackHeader getPack(int packIdx);

		@Override
		abstract public int getPackRootIdx();

		abstract protected BinaryReader getBinaryReader();

	}

	private final class CachedIndexFile extends BaseIndexFile {
		private final FileAccessCache cache;

		private final StructArchiveFile archiveHeader;
		private final StructPackHeader[] packs;
		private final StructAIDX aidx;

		public CachedIndexFile(FileAccessCache cache) {
			super();
			this.cache = cache;
			try {
				final BinaryReader reader = cache.getReader();
				this.archiveHeader = loadHeader(structReader, reader);
				this.packs = loadPackHeader(structReader, reader, archiveHeader);
				this.aidx = loadAIDX(structReader, reader, archiveHeader, packs);
			} catch (final IOException e) {
				throw new IllegalStateException(e); // TODO
			}
		}

		@Override
		public int getPackCount() {
			return (int) archiveHeader.packCount;
		}

		@Override
		public int getPackRootIdx() {
			return aidx.rootPackHeaderIdx;
		}

		@Override
		public List<StructIdxEntry> getDirectoryData(int packIdx) {
			final List<StructIdxEntry> entries = super.getDirectoryData(packIdx);
			cache.startExpiring();
			return entries;
		}

		@Override
		protected BinaryReader getBinaryReader() {
			try {
				return cache.getReader();
			} catch (final IOException e) {
				throw new IllegalStateException(e); // TODO
			}
		}

		@Override
		public StructPackHeader getPack(int packIdx) {
			return packs[packIdx];
		}

	}

	public IndexFile read(final Path indexPath) {
		final FileAccessCache cache = new FileAccessCache(60000, indexPath, 4 * 1024 * 1024, ByteOrder.LITTLE_ENDIAN);
		return new CachedIndexFile(cache);
	}

	public Index2File read(final BinaryReader reader) {
		if ((reader == null) || !reader.isOpen()) {
			throw new IllegalArgumentException();
		}

		final DataReadDelegator<BinaryReader> readerDelegator = DataReadDelegator.build(new kreed.reflection.struct.reader.BinaryReader());
		final StructReader<BinaryReader> structReader = StructReader.build(StructFactory.build(), readerDelegator, true);

		final StructArchiveFile archiveHeader = loadHeader(structReader, reader);
		final StructPackHeader[] packs = loadPackHeader(structReader, reader, archiveHeader);
		final StructAIDX aidx = loadAIDX(structReader, reader, archiveHeader, packs);

		return new Index2File(archiveHeader, packs, aidx);
	}

	private StructArchiveFile loadHeader(StructReader<BinaryReader> structReader, BinaryReader reader) {
		final StructArchiveFile archiveHeader = structReader.read(StructArchiveFile.class, reader);
		if (archiveHeader.signature != StructArchiveFile.SIGNATURE_INDEX) {
			throw new SignatureMismatchException("Index file", StructArchiveFile.SIGNATURE_INDEX, archiveHeader.signature);
		}
		if (archiveHeader.version != 1) {
			throw new VersionMismatchException("Index file", 1, archiveHeader.version);
		}
		return archiveHeader;
	}

	private StructPackHeader[] loadPackHeader(StructReader<BinaryReader> structReader, BinaryReader reader, StructArchiveFile archiveHeader) {
		if (archiveHeader.packOffset < 0) {
			throw new IntegerOverflowException("Index file: pack offset");
		}

		if ((archiveHeader.packCount > Integer.MAX_VALUE) || (archiveHeader.packCount < 0)) {
			throw new IntegerOverflowException("Index file: pack count");
		}

		reader.seek(Seek.BEGIN, archiveHeader.packOffset);
		final StructPackHeader[] pack = new StructPackHeader[(int) archiveHeader.packCount];
		for (int i = 0; i < pack.length; ++i) {
			pack[i] = structReader.read(new StructPackHeader(), reader);
		}
		return pack;
	}

	private StructAIDX loadAIDX(StructReader<BinaryReader> structReader, BinaryReader reader, StructArchiveFile archiveHeader, StructPackHeader[] packs) {
		if (archiveHeader.packRootIdx > archiveHeader.packCount) {
			throw new IllegalArgumentException(
					String.format("Index File : Pack root idx %d exceeds pack count %d", archiveHeader.packRootIdx, archiveHeader.packCount));
		}
		if ((archiveHeader.packRootIdx > Integer.MAX_VALUE) || (archiveHeader.packRootIdx < 0)) {
			throw new IntegerOverflowException("Index file: pack root");
		}

		final StructPackHeader aidxpack = packs[(int) archiveHeader.packRootIdx];
		reader.seek(Seek.BEGIN, aidxpack.getOffset());

		final StructAIDX aidx = structReader.read(new StructAIDX(), reader);
		if (aidx.signature != StructAIDX.SIGNATURE_AIDX) {
			throw new SignatureMismatchException("Index file: AIDX block", StructAIDX.SIGNATURE_AIDX, aidx.signature);
		}

		return aidx;
	}

}
