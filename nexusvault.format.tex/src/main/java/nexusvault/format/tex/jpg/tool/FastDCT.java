package nexusvault.format.tex.jpg.tool;

public final class FastDCT {

	private static final double[] coefficients = new double[64];
	private static final int N = 8;
	private static final double SCALE = 8d;

	static {
		for (var k = 0; k < N; ++k) {
			for (var n = 0; n < N; ++n) {
				coefficients[n + k * N] = Math.cos(Math.PI * (2 * n + 1) * k / SCALE);
			}
		}
	}

	/**
	 *
	 * @param data
	 *            row-major matrix 8x8
	 * @param dataOffset
	 */
	public static void dct(int[] data, int dataOffset) {
		final var buffer = new int[data.length];
		final var sumScale = 1 / SCALE;

		// TODO this can still be optimized

		for (var k1 = 0; k1 < N; ++k1) {
			for (var k2 = 0; k2 < N; ++k2) {
				double sum = 0;
				for (var n1 = 0; n1 < N; ++n1) {
					for (var n2 = 0; n2 < N; ++n2) {
						sum += data[n1 * N + n2 + dataOffset] * coefficients[n1 + k1 * N] * coefficients[n2 + k2 * N];
					}
				}
				buffer[k1 * N + k2] = (int) Math.round(sumScale * sum);
			}
		}

		System.arraycopy(buffer, 0, data, dataOffset, buffer.length);
	}

	private static void dctRows(int[] data, int dataOffset) {
		final var tmp = new double[8];
		for (var a = 0; a < 64; a += 8) { // points to row start
			for (var n = 0; n < 8; ++n) { // element on which to apply dct
				tmp[n] = 0;
				for (var i = 0; i < 8; ++i) { // iterate over row
					tmp[n] += data[dataOffset + i] * coefficients[i + n * 8];
				}
			}

			for (var n = 0; n < 8; ++n) {
				data[a + n] = (int) Math.round(tmp[n]);
			}
		}
	}

	private static void dctColumns(int[] data, int dataOffset) {
		final var tmp = new double[8];
		for (var a = 0; a < 64; a += 8) { // points to row start
			for (var n = 0; n < 8; ++n) { // element on which to apply dct
				tmp[n] = 0;
				for (var i = 0; i < 8; ++i) { // iterate over row
					tmp[n] += data[dataOffset + a + i] * coefficients[a + n];
				}
			}

			for (var n = 0; n < 8; ++n) {
				data[a + n] = (int) Math.round(tmp[n]);
			}
		}
	}

}
