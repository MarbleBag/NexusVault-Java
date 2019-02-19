package nexusvault.archive.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import kreed.io.util.BinaryReader;
import nexusvault.archive.ArchiveDecodeException;
import nexusvault.archive.ArchiveEncodeException;
import nexusvault.shared.exception.IntegerOverflowException;

final class LZMACodec implements ArchiveDecoder, ArchiveEncoder {

	public ByteBuffer decode(BinaryReader data, long compressedSize, long uncompressedSize) throws ArchiveDecodeException {
		if ((compressedSize < 0) || (compressedSize > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException();
		}
		if ((uncompressedSize < 0) || (uncompressedSize > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException();
		}

		final byte[] lzmaProperties = new byte[5];
		final byte[] dataArray = new byte[(int) (compressedSize - lzmaProperties.length)];

		data.readInt8(lzmaProperties, 0, lzmaProperties.length);
		data.readInt8(dataArray, 0, dataArray.length);

		final SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
		decoder.SetDecoderProperties(lzmaProperties);

		final ByteArrayInputStream encoded = new ByteArrayInputStream(dataArray);
		final ByteArrayOutputStream decoded = new ByteArrayOutputStream((int) uncompressedSize);

		try {
			final boolean decodeResult = decoder.Code(encoded, decoded, uncompressedSize);
			if (!decodeResult) {
				throw new ArchiveDecodeException("LZMA uncompression error");
			}
		} catch (final IOException e) {
			throw new ArchiveDecodeException(e);
		}

		final ByteBuffer result = ByteBuffer.wrap(decoded.toByteArray());
		return result;
	}

	public ByteBuffer encode(BinaryReader data) throws ArchiveEncodeException {
		final SevenZip.Compression.LZMA.Encoder encoder = new SevenZip.Compression.LZMA.Encoder();

		final byte[] dataArray = new byte[(int) data.size()];
		data.readInt8(dataArray, 0, dataArray.length);

		final ByteArrayInputStream decoded = new ByteArrayInputStream(dataArray);
		final ByteArrayOutputStream encoded = new ByteArrayOutputStream(2 * 1024);

		try {
			encoder.WriteCoderProperties(encoded);
			final long fileSize = data.size();
			for (int i = 0; i < 8; i++) {
				encoded.write((int) ((fileSize >>> (8 * i)) & 0xFF));
			}
			encoder.Code(decoded, encoded, -1, -1, null);
		} catch (final IOException e) {
			throw new ArchiveEncodeException(e);
		}

		final ByteBuffer result = ByteBuffer.wrap(encoded.toByteArray());
		return result;
	}

}
