package nexusvault.shared.util;

public final class ByteAlign {
	private ByteAlign() {
	}

	public static long alignTo16Byte(long position) {
		return position + 15 & 0xFFFFFFFFFFFFF0L;
	}

	public static long advanceByToAlign16Byte(long position) {
		return alignTo16Byte(position) - position;
	}

	public static int alignTo16Byte(int position) {
		return position + 15 & 0xFFFFF0;
	}

	public static int advanceByToAlign16Byte(int position) {
		return alignTo16Byte(position) - position;
	}

	public static long alignTo8Byte(long position) {
		return position + 7 & 0xFFFFFFFFFFFFF8L;
	}

	public static long advanceByToAlign8Byte(long position) {
		return alignTo8Byte(position) - position;
	}

	public static int alignTo8Byte(int position) {
		return position + 7 & 0xFFFFF8;
	}

	public static int advanceByToAlign8Byte(int position) {
		return alignTo8Byte(position) - position;
	}

}
