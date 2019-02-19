package nexusvault.archive.impl;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveDecodeException;
import nexusvault.shared.exception.IntegerOverflowException;

final class ZipCodec implements ArchiveDecoder, ArchiveEncoder {

	public ByteBuffer decode(BinaryReader reader, long compressedSize, long uncompressedSize) throws ArchiveDecodeException {
		try {
			if ((compressedSize < 0) || (compressedSize > Integer.MAX_VALUE)) {
				throw new IntegerOverflowException();
			}
			if ((uncompressedSize < 0) || (uncompressedSize > Integer.MAX_VALUE)) {
				throw new IntegerOverflowException();
			}

			final byte[] rawData = new byte[(int) compressedSize];
			reader.readInt8(rawData, 0, rawData.length);

			final Inflater inflater = new java.util.zip.Inflater();
			inflater.setInput(rawData);
			inflater.finished();

			final byte[] output = new byte[(int) uncompressedSize];
			final int resultLength = inflater.inflate(output);

			inflater.end();

			if (output.length != resultLength) {
				throw new ArchiveDecodeException("ZLIB: Uncompressed Size does not match expecations. Got " + resultLength + " expected " + output.length);
			}

			return ByteBuffer.wrap(output);

		} catch (final DataFormatException e) {
			throw new ArchiveDecodeException(e);
		}
	}

}
