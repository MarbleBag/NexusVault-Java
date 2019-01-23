package nexusvault.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteUtil {

	public static String byteToHex(byte[] arr) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : arr) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	static final int toMagicNumber(String in) {
		final byte[] data = in.getBytes(Charset.forName("UTF8"));
		if (data.length > 4) {
			throw new IllegalArgumentException();
		}

		int result = 0;
		for (int i = 0; i < data.length; ++i) {
			final int toStore = data[i];
			result |= (toStore << (8 * (data.length - 1 - i)));
		}

		return result;
	}

	static final String fromMagicNumber(int in) {
		final Charset charset = StandardCharsets.UTF_8;
		final byte[] data = new byte[4];
		int firstNon0 = -1;
		int lastNon0 = 0;
		for (int i = 0; i < data.length; ++i) {
			data[i] = (byte) ((in >>> (8 * (data.length - i - 1))) & 0xFF);
			if (data[i] != 0) {
				if (firstNon0 == -1) {
					firstNon0 = i;
				}
				lastNon0 = Math.max(lastNon0, i + 1);
			}
		}
		if ((firstNon0 < 0) || ((lastNon0 - firstNon0) == 0)) {
			return "";
		}
		final byte[] nString = new byte[lastNon0 - firstNon0];
		System.arraycopy(data, firstNon0, nString, 0, lastNon0 - firstNon0);
		return new String(nString, charset);
	}

	static final int toInt(byte[] in) {
		int result = 0;
		for (int i = 0; i < in.length; ++i) {
			result |= (in[i] << (8 * (in.length - 1 - i)));
		}
		return result;
	}

	static final int toInt(String in) {
		final byte[] data = in.getBytes(Charset.forName("UTF8"));
		if (data.length > 4) {
			throw new IllegalArgumentException();
		}

		int result = 0;
		for (int i = 0; i < data.length; ++i) {
			result |= (data[i] << (8 * (data.length - 1 - i)));
		}

		return result;
	}

	static final String toASCII(int in, int bytesPerChar) {
		Charset charset;
		switch (bytesPerChar) {
			case 1:
				charset = StandardCharsets.UTF_8;
				break;
			case 2:
				charset = StandardCharsets.UTF_16LE;
				break;
			default:
				throw new IllegalArgumentException("Supported bytes per char: 1, 2");
		}
		final byte[] data = new byte[4];
		for (int i = 0; i < data.length; ++i) {
			data[i] = (byte) ((in >>> (8 * (data.length - i - 1))) & 0xFF);
		}
		return new String(data, charset);
	}

	static String toBinary(int i) {
		return String.format("%32s", Integer.toBinaryString(i)).replace(" ", "0");
	}

	static String toBinary(short i) {
		return String.format("%16s", Integer.toBinaryString(i & 0xFFFF)).replace(" ", "0");
	}

	static String toBinary(byte i) {
		return String.format("%8s", Integer.toBinaryString(i & 0xFF)).replace(" ", "0");
	}

	static String toBinary(byte[] i) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : i) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(toBinary(b));
		}
		return sb.toString();
	}

}
