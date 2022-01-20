package nexusvault.shared;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;

public final class Text {
	private Text() {

	}

	public static String decodeUTF16(ByteOrder order, byte[] data) {
		final Charset charset = getUTF16Charset(order);
		final CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(data));
		return charBuffer.toString();
	}

	public static String readUTF16(BinaryReader reader, long bytes) {
		final var data = new byte[(int) bytes];
		reader.readInt8(data, 0, data.length);
		return decodeUTF16(reader.byteOrder(), data);
	}

	public static String readNullTerminatedUTF8(BinaryReader reader) {
		// small buffer, just big enough to digest most small strings in one go
		final var inputBuffer = ByteBuffer.allocate(Short.BYTES * 64).order(reader.byteOrder());
		final var outputBuffer = CharBuffer.allocate(64);
		final var builder = new StringBuilder(128);
		final var decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
		while (!reader.isEndOfData()) {
			final var character = reader.readInt8();
			if (character == 0) {
				break; // terminated
			}
			inputBuffer.put(character);

			if (!inputBuffer.hasRemaining()) {
				inputBuffer.flip();
				decoder.decode(inputBuffer, outputBuffer, false);
				outputBuffer.flip();
				builder.append(outputBuffer.toString());
				outputBuffer.rewind();
				inputBuffer.compact();
			}
		}

		inputBuffer.flip();
		CoderResult decodeResult;
		do {
			decodeResult = decoder.decode(inputBuffer, outputBuffer, true);
			outputBuffer.flip();
			builder.append(outputBuffer.toString());
			outputBuffer.rewind();
		} while (decodeResult.isOverflow());

		return builder.toString();
	}

	public static String readNullTerminatedUTF16(BinaryReader reader) {
		// small buffer, just big enough to digest most small strings in one go
		final var inputBuffer = ByteBuffer.allocate(Short.BYTES * 64).order(reader.byteOrder());
		final var outputBuffer = CharBuffer.allocate(64);
		final var builder = new StringBuilder(128);

		final var charset = getUTF16Charset(reader.byteOrder());
		final var decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);

		while (!reader.isEndOfData()) {
			final short character = reader.readInt16();
			if (character == 0) {
				break; // terminated
			}
			inputBuffer.putShort(character);

			if (!inputBuffer.hasRemaining()) {
				inputBuffer.flip();
				decoder.decode(inputBuffer, outputBuffer, false);
				outputBuffer.flip();
				builder.append(outputBuffer.toString());
				outputBuffer.rewind();
				inputBuffer.compact();
			}
		}

		inputBuffer.flip();
		CoderResult decodeResult;
		do {
			decodeResult = decoder.decode(inputBuffer, outputBuffer, true);
			outputBuffer.flip();
			builder.append(outputBuffer.toString());
			outputBuffer.rewind();
		} while (decodeResult.isOverflow());

		return builder.toString();
	}

	public static byte[] encodeAsUTF16(ByteOrder order, String text) {
		final Charset charset = getUTF16Charset(order);
		final ByteBuffer encoded = charset.encode(text);
		return encoded.array();
	}

	public static int writeUTF16(BinaryWriter writer, String text) {
		final var encoded = encodeAsUTF16(writer.byteOrder(), text);
		writer.writeInt8(encoded, 0, encoded.length);
		return encoded.length;
	}

	public static int writeNullTerminatedUTF16(BinaryWriter writer, String text) {
		final var length = writeUTF16(writer, text);
		writer.writeInt16(0);
		return length + 2;
	}

	public static int writeNullTerminatedUTF16(BinaryWriter writer, byte[] encoded) {
		writer.writeInt8(encoded, 0, encoded.length);
		writer.writeInt16(0);
		return encoded.length + 2;
	}

	private static Charset getUTF16Charset(ByteOrder order) {
		if (ByteOrder.LITTLE_ENDIAN.equals(order)) {
			return StandardCharsets.UTF_16LE;
		} else {
			return StandardCharsets.UTF_16BE;
		}
	}

}
