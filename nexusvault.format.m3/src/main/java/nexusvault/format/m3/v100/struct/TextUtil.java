package nexusvault.format.m3.v100.struct;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import kreed.io.util.BinaryReader;

// TODO move to shared
public abstract class TextUtil {
	private TextUtil() {

	}

	public static String extractUTF16(BinaryReader reader, long bytes) {
		final Charset charset = findCharset(reader);
		final byte[] data = new byte[(int) bytes];
		reader.readInt8(data, 0, data.length);
		final CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(data));
		return charBuffer.toString();
	}

	public static String extractNullTerminatedUTF16(BinaryReader reader) {

		final ByteBuffer buffer = ByteBuffer.allocate(64); // small buffer, just big enough to digest most small strings in one go
		final StringBuilder builder = new StringBuilder(128);

		final Charset charset = findCharset(buffer);
		boolean isTerminated = false;
		while (!isTerminated && !reader.isEndOfData()) {
			final short character = reader.readInt16();
			isTerminated = character == 0;
			if (!isTerminated) {
				buffer.putShort(character);
			}

			if (!buffer.hasRemaining()) {
				buffer.flip();
				final CharBuffer tmp = charset.decode(buffer);
				builder.append(tmp.toString());
				buffer.rewind();
			}
		}

		buffer.flip();
		if (buffer.hasRemaining()) {
			final CharBuffer tmp = charset.decode(buffer);
			builder.append(tmp.toString());
		}

		return builder.toString();
	}

	private static Charset findCharset(ByteBuffer buffer) {
		return findCharset(buffer.order());
	}

	private static Charset findCharset(BinaryReader reader) {
		return findCharset(reader.getOrder());
	}

	private static Charset findCharset(ByteOrder order) {
		if (ByteOrder.LITTLE_ENDIAN.equals(order)) {
			return StandardCharsets.UTF_16LE;
		} else {
			return StandardCharsets.UTF_16BE;
		}
	}

}
