package nexusvault.vault.archive;

public final class HexToString {

	public static String byteToHex(byte[] arr) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : arr) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	public static String toBinary(int i) {
		return String.format("%32s", Integer.toBinaryString(i)).replace(" ", "0");
	}

	public static String toBinary(short i) {
		return String.format("%16s", Integer.toBinaryString(i & 0xFFFF)).replace(" ", "0");
	}

	public static String toBinary(byte i) {
		return String.format("%8s", Integer.toBinaryString(i & 0xFF)).replace(" ", "0");
	}

	public static String toBinary(byte[] i) {
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
