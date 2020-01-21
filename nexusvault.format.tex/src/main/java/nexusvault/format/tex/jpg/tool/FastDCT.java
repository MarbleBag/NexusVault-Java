package nexusvault.format.tex.jpg.tool;

public final class FastDCT {

	private static final double[] coefficients = new double[64];

	static {
		// for (int y = 0; y < 8; y++) {
		// for (int x = 0; x < 8; x++) {
		// coefficients[x + y * 8] = Math.cos((2f * y + 1f) * x * (Math.PI / 64f));
		// }
		// }

		for (var j = 0; j < 8; ++j) {
			coefficients[j] = Math.sqrt(0.125);
			for (var i = 8; i < 64; i += 8) {
				coefficients[i + j] = 0.5 * Math.cos(i * (j + 0.5) * Math.PI / 64.0);
			}
		}

		// coefficients depend on k & n, but both values are bound and limited to [0,7]. So precalculate all 64 possible coefficients values to speed things up
		// for (var k = 0; k < 8; ++k) { // row
		// for (var n = 0; n < 8; ++n) { // column
		// coefficients[n + k * 8] = Math.cos(Math.PI / 8d * (n + 0.5) * k);
		// }
		// }
	}

	/**
	 *
	 * @param data
	 *            row-major matrix 8x8
	 * @param dataOffset
	 */
	public static void dct(int[] data, int dataOffset) {
		// final var buffer = new int[64];
		// final var DCT = new DCTLookup(8, 8);
		// for (int y0 = 0; y0 < Constants.BLOCK_HEIGHT; ++y0) {
		// for (int x0 = 0; x0 < Constants.BLOCK_WIDTH; ++x0) {
		// final double value = DCT.DCT(x0, y0, data, dataOffset, Constants.BLOCK_WIDTH, Constants.BLOCK_HEIGHT);
		// buffer[x0 + y0 * Constants.BLOCK_WIDTH] = (int) Math.round(value);
		// }
		// }
		// System.arraycopy(buffer, 0, data, dataOffset, Constants.BLOCK_SIZE);

		final var out = new double[64];

		/* out = coefficients * block */
		for (var i = 0; i < 64; i += 8) {
			for (var j = 0; j < 8; ++j) {
				double tmp = 0;
				for (var k = 0; k < 8; ++k) {
					tmp += coefficients[i + k] * data[dataOffset + k * 8 + j];
				}
				out[i + j] = tmp * 8;
			}
		}

		/* block = out * (coefficients') */
		for (var j = 0; j < 8; ++j) {
			for (var i = 0; i < 64; i += 8) {
				double tmp = 0;
				for (var k = 0; k < 8; ++k) {
					tmp += out[i + k] * coefficients[j * 8 + k];
				}
				data[dataOffset + i + j] = (int) Math.floor(tmp + 0.499999999999);
			}
		}

		// dctRows(data, dataOffset);
		// dctColumns(data, dataOffset);
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
