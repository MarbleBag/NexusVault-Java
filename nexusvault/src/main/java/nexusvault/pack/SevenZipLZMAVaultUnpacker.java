package nexusvault.pack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import kreed.io.util.BinaryReader;
import nexusvault.shared.exception.IntegerOverflowException;

final class SevenZipLZMAVaultUnpacker implements VaultUnpacker {

	@Override
	public ByteBuffer unpack(BinaryReader channel, long compressedSize, long uncompressedSize) {
		if ((compressedSize < 0) || (compressedSize > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException();
		}
		if ((uncompressedSize < 0) || (uncompressedSize > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException();
		}

		final byte[] lzmaProperties = new byte[5];
		final byte[] rawData = new byte[(int) (compressedSize - lzmaProperties.length)];

		channel.readInt8(lzmaProperties, 0, lzmaProperties.length);
		channel.readInt8(rawData, 0, rawData.length);

		final SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
		decoder.SetDecoderProperties(lzmaProperties);

		final ByteArrayInputStream encoded = new ByteArrayInputStream(rawData);
		final ByteArrayOutputStream decoded = new ByteArrayOutputStream((int) uncompressedSize);

		try {
			final boolean decodeResult = decoder.Code(encoded, decoded, uncompressedSize);
			if (!decodeResult) {
				throw new VaultUnpackException("LZMA uncompression error");
			}
		} catch (final IOException e) {
			throw new VaultUnpackException(e);
		}

		final ByteBuffer result = ByteBuffer.wrap(decoded.toByteArray());
		return result;
	}

}
