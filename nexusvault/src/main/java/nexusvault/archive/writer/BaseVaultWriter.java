package nexusvault.archive.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import kreed.io.util.BinaryWriter;
import kreed.io.util.Seek;
import kreed.io.util.WritableByteChannelBinaryWriter;
import kreed.reflection.struct.DataWriteDelegator;
import kreed.reflection.struct.StructBuilder;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructWriter;
import nexusvault.archive.IdxDirectory;
import nexusvault.archive.IdxEntry;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.IdxPath;
import nexusvault.archive.struct.StructArchiveFile;
import nexusvault.archive.struct.StructIndexFile;
import nexusvault.archive.struct.StructPackHeader;

public class BaseVaultWriter {

	/**
	 * Describes an element that goes into the archive
	 */
	public static interface IntegrateableElement {
		IdxPath getDestination();

		ByteBuffer getData();
	}

	public void toVault(Path baseDirectory /* not included */) {
		baseDirectory.compareTo(null);
	}

	public void toVault(List<IntegrateableElement> elements, Path outputDirectory, String fileName) throws IOException {

		writeArchive(elements, outputDirectory, fileName);

	}

	private void writeIndex(List<IntegrateableElement> elements, BinaryWriter writer) {
		final StructBuilder structBuilder = StructBuilder.build(StructFactory.build());
		final DataWriteDelegator<BinaryWriter> writeDelegator = DataWriteDelegator.build(new kreed.reflection.struct.writer.BinaryWriter());
		final StructWriter<BinaryWriter> structWriter = StructWriter.build(StructFactory.build(), writeDelegator, true);

		final StructIndexFile header = structBuilder.build(StructIndexFile.class);
		header.signature = ('P' << 24) | ('A' << 16) | ('C' << 8) | 'K';
		header.version = 1;

		final IdxDirectory root = buildFileTree(elements);

	}

	private static abstract class IdxDirectoryBuilder extends IdxEntryBuilder implements IdxDirectory {

		public IdxDirectoryBuilder createDirectories(IdxPath path) {
			IdxDirectoryBuilder dir = this;
			for (int i = 0; i < path.depth(); ++i) {
				final String directoryName = path.getNameOf(i);
				if (dir.hasEntry(directoryName)) {
					final IdxEntryBuilder entry = (IdxEntryBuilder) dir.getEntry(directoryName);
					if (!entry.isDir()) {
						// TODO ERROR
					} else {
						dir = (IdxDirectoryBuilder) entry.asDirectory();
					}
				} else {
					dir = dir.createDirectory(directoryName);
				}
			}
			return dir;
		}

		public IdxDirectoryBuilder createDirectory(String directoryName) {
			// TODO Auto-generated method stub
			return null;
		}

		public IdxFileLinkBuilder createFile(String lastName) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private static abstract class IdxFileLinkBuilder extends IdxEntryBuilder implements IdxFileLink {

	}

	private static abstract class IdxEntryBuilder implements IdxEntry {

	}

	private IdxDirectory buildFileTree(List<IntegrateableElement> elements) {

		final IdxDirectoryBuilder root = new IdxDirectoryBuilder();

		for (final IntegrateableElement element : elements) {
			final IdxPath p = element.getDestination();
			IdxDirectoryBuilder dir = root;
			if (p.hasParent()) {
				dir = dir.createDirectories(p.getParent());
			}
			final IdxFileLinkBuilder fileLink = dir.createFile(p.getLastName());
			// TODO
		}

		// TODO Auto-generated method stub
		return null;
	}

	private void writeBlock(BinaryWriter writer, ByteBuffer block) {
		final long blockSize = block.remaining();

		final long writerPosStart = writer.getPosition();
		final long writerPosStartPad = padPosition(writerPosStart);
		final long writerPosEnd = writerPosStart + blockSize;
		final long writerPosEndPad = padPosition(writerPosEnd);

		if (writerPosStart != writerPosStartPad) {
			throw new IllegalStateException("Block is not aligned at 16-byte boundary");
		}

		final long blockSizePad = writerPosEndPad - writerPosStartPad;
		writer.seek(Seek.CURRENT, -8);
		writer.writeInt64(blockSizePad);
		writer.write(block);
		writer.writeInt64(blockSizePad);
	}

	private void writeEmptyBlock(BinaryWriter writer, int blockSize) {
		final long writerPosStart = writer.getPosition();
		final long writerPosStartPad = padPosition(writerPosStart);
		final long writerPosEnd = writerPosStart + blockSize;
		final long writerPosEndPad = padPosition(writerPosEnd);

		if (writerPosStart != writerPosStartPad) {
			throw new IllegalStateException("Block is not aligned at 16-byte boundary");
		}

		final long blockSizePad = writerPosEndPad - writerPosStartPad;
		writer.seek(Seek.CURRENT, -8);
		writer.writeInt64(-blockSizePad);
		writer.seek(Seek.CURRENT, blockSizePad);
		writer.writeInt64(-blockSizePad);
	}

	private void writeArchive(List<IntegrateableElement> elements, Path outputDirectory, String fileName) throws IOException {
		// create archive
		final Path archivePath = outputDirectory.resolve(fileName + ".archive");
		Files.createDirectories(outputDirectory);
		try (SeekableByteChannel archiveChannel = Files.newByteChannel(archivePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {

			final BinaryWriter writer = new WritableByteChannelBinaryWriter(archiveChannel, ByteBuffer.allocate(10 * 1024 * 1024));
			final StructBuilder structBuilder = StructBuilder.build(StructFactory.build());
			final DataWriteDelegator<BinaryWriter> writeDelegator = DataWriteDelegator.build(new kreed.reflection.struct.writer.BinaryWriter());
			final StructWriter<BinaryWriter> structWriter = StructWriter.build(StructFactory.build(), writeDelegator, true);

			final StructArchiveFile header = structBuilder.build(StructArchiveFile.class);
			header.signature = ('P' << 24) | ('A' << 16) | ('C' << 8) | 'K';
			header.version = 1;

			// reserve header space
			structWriter.write(header, writer); // needs to be updated at the end of the process

			// guard stop
			writer.writeInt64(0l);

			// a pack header measures exactly 16 bytes per entry.
			// step 1: calculate pack header space for all entries.
			{ // pack
				final long blockSize = elements.size() * StructPackHeader.SIZE_IN_BYTES;
				// 'in case': check for padding
				final long blockSizePad = padPosition(0);
				writer.writeInt64(blockSize);
			}
		}
	}

	private static long padPosition(long pos) {
		return (pos + 0xF) & 0xFFFFFFFFFFFFFFF0l;
	}

}
