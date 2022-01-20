package nexusvault.vault.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferInputStream;
import nexusvault.shared.exception.IntegerOverflowException;

public final class LzmaCodec {
	private LzmaCodec() {
	}

	public static byte[] decode(BinaryReader reader, int inputLength, long uncompressedSize) throws DecodeException {
		final var data = new byte[inputLength];
		reader.readInt8(data, 0, data.length);
		return decode(data, uncompressedSize);
	}

	public static byte[] decode(ByteBuffer data, long uncompressedSize) throws DecodeException {
		final var lzmaProperties = new byte[5];
		data.get(lzmaProperties);
		final var encoded = new ByteBufferInputStream(data);
		return decode(lzmaProperties, encoded, uncompressedSize);
	}

	public static byte[] decode(byte[] data, long uncompressedSize) throws DecodeException {
		final var lzmaProperties = new byte[5];
		System.arraycopy(data, 0, lzmaProperties, 0, lzmaProperties.length);
		final var encoded = new ByteArrayInputStream(data, lzmaProperties.length, data.length - lzmaProperties.length);
		return decode(lzmaProperties, encoded, uncompressedSize);
	}

	public static byte[] decode(byte[] lzmaProperties, InputStream encoded, long uncompressedSize) throws DecodeException {
		if (uncompressedSize < 0 || uncompressedSize > Integer.MAX_VALUE) {
			throw new IntegerOverflowException();
		}

		final var decoder = new SevenZip.Compression.LZMA.Decoder();
		decoder.SetDecoderProperties(lzmaProperties);

		final var decoded = new ByteArrayOutputStream();

		try {
			final var success = decoder.Code(encoded, decoded, uncompressedSize);
			if (!success) {
				throw new DecodeException("LZMA uncompression error");
			}
		} catch (final IOException e) {
			throw new DecodeException(e);
		}

		return decoded.toByteArray();
	}

	public static byte[] encode(byte[] data) throws EncodeException {
		return encode(new ByteArrayInputStream(data), data.length);
	}

	public static byte[] encode(ByteBuffer data) throws EncodeException {
		return encode(new ByteBufferInputStream(data), data.remaining());
	}

	public static byte[] encode(BinaryReader reader, int inputLength) throws EncodeException {
		final var data = new byte[inputLength];
		reader.readInt8(data, 0, data.length);
		return encode(data);
	}

	public static byte[] encode(InputStream decoded, int inputLength) throws EncodeException {
		final var encoded = new ByteArrayOutputStream(inputLength);
		try {
			final var encoder = new SevenZip.Compression.LZMA.Encoder();
			encoder.WriteCoderProperties(encoded);
			encoder.Code(decoded, encoded, -1, -1, null);
		} catch (final IOException e) {
			throw new EncodeException(e);
		}
		return encoded.toByteArray();
	}

}
