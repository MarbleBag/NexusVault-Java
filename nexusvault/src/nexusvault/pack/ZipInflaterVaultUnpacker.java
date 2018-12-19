package nexusvault.pack;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import kreed.io.util.BinaryReader;
import nexusvault.shared.exception.IntegerOverflowException;

final class ZipInflaterVaultUnpacker implements VaultUnpacker {

	@Override
	public ByteBuffer unpack(BinaryReader reader, long compressedSize, long uncompressedSize) throws VaultUnpackException {
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
				throw new VaultUnpackException("ZLIB: Uncompressed Size does not match expecations. Got " + resultLength + " expected " + output.length);
			}

			return ByteBuffer.wrap(output);

		} catch (final DataFormatException e) {
			throw new VaultUnpackException(e);
		}
	}

}
