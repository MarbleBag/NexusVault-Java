package nexusvault.archive.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.archive.ArchiveEntryNotFoundException;
import nexusvault.archive.IdxFileLink;
import nexusvault.archive.VaultReader;
import nexusvault.shared.exception.IntegerOverflowException;

abstract class AbstVaultReader implements VaultReader {

	private final VaultUnpacker UNPACK_ZIP = new ZipInflaterVaultUnpacker();
	private final VaultUnpacker UNPACK_LZMA = new SevenZipLZMAVaultUnpacker();

	public AbstVaultReader() {

	}

	abstract void startArchiveAccess() throws IOException;

	abstract void endArchiveAccess();

	abstract BinaryReader getArchiveBinaryReader() throws IOException;

	abstract PackHeader getPackHeaderForFile(IdxFileLink file) throws ArchiveEntryNotFoundException;

	@Override
	final public ByteBuffer getData(IdxFileLink file) throws IOException, ArchiveEntryNotFoundException {
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
