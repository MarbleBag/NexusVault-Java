package nexusvault.archive.impl;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveDecodeException;
import nexusvault.archive.ArchiveEncodeException;
import nexusvault.shared.exception.IntegerOverflowException;

final class ZipCodec {

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

			final byte[] output = new byte[(int) uncompressedSize];
			final int resultLength = inflater.inflate(output);

			inflater.end();

			if (output.length != resultLength) {
				throw new ArchiveDecodeException("ZLIB: Uncompressed Size does not match expecations. Got " + resultLength + " expected " + output.length);
			}

			return ByteBuffer.wrap(output).order(reader.getOrder());

		} catch (final DataFormatException e) {
			throw new ArchiveDecodeException(e);
		}
	}

	public ByteBuffer encode(BinaryReader reader) throws ArchiveEncodeException {
		final byte[] rawData = new byte[(int) reader.size()];
		reader.readInt8(rawData, 0, rawData.length);

		final Deflater deflater = new java.util.zip.Deflater();
		deflater.setInput(rawData);
		deflater.finish();

		final ByteArrayOutputStream encoded = new ByteArrayOutputStream(Math.min((int) reader.size(), 2 << 10));
		final byte[] buffer = new byte[Math.min((int) reader.size(), 2 << 10)];

		try {
			while (!deflater.finished()) {
				final int writtenBytes = deflater.deflate(buffer);
				encoded.write(buffer, 0, writtenBytes);
			}
		} catch (final Throwable e) {
			throw new ArchiveEncodeException(e);
		}

		final ByteBuffer result = ByteBuffer.wrap(encoded.toByteArray()).order(reader.getOrder());
		return result;
	}

}
