package nexusvault.format.tex.jpg.tools;

public final class MathHelper {
	private MathHelper() {
	}

	public static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public static int sqrtInteger(int x) {
		if (x == 0) {
			return 0;
		}
		final int low = 2 * sqrtInteger(x / 4);
		final int high = low + 1;
		if (x < high * high) {
			return low;
		}
		return high;
	}

}
