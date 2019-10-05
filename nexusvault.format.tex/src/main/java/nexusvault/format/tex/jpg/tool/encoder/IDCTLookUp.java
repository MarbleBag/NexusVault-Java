package nexusvault.format.tex.jpg.tool.encoder;

final class IDCTLookUp {

	private static final double Cx_sqr = 1 / Math.sqrt(2);

	private final double[] lookup;
	private final int max;

	public IDCTLookUp(int rows, int columns) {
		max = Math.max(rows, columns);
		lookup = new double[max * max];
		for (int x0 = 0; x0 < max; x0++) {
			for (int x = 0; x < max; x++) {
				lookup[x + (x0 * max)] = Math.cos((((2f * x0) + 1f) * x * Math.PI) / 16f);
			}
		}
	}

	public double cof(int x, int x0) {
		return lookup[x + (x0 * max)];
	}

	public double C(int n) {
		return n == 0 ? Cx_sqr : 1;
	}

	public double IDCT(int x0, int y0, int[] matrix, int offset, int rows, int columns) {
		if (max < Math.max(rows, columns)) {
			throw new IndexOutOfBoundsException();
		}
		if ((x0 < 0) || (y0 < 0) || (max < x0) || (max < y0) || (rows <= 0) || (columns <= 0)) {
			throw new IndexOutOfBoundsException();
		}

		double accu = 0;
		for (int y = 0; y < rows; ++y) {
			for (int x = 0; x < columns; ++x) {
				accu += C(x) * C(y) * cof(x, x0) * cof(y, y0) * matrix[offset + x + (y * columns)];
			}
		}
		return (1f / 4f) * accu;
	}

	public double IDCT(int x0, int y0, short[] matrix, int offset, int rows, int columns) {
		if (max < Math.max(rows, columns)) {
			throw new IndexOutOfBoundsException();
		}
		if ((x0 < 0) || (y0 < 0) || (max < x0) || (max < y0) || (rows <= 0) || (columns <= 0)) {
			throw new IndexOutOfBoundsException();
		}

		double accu = 0;
		for (int y = 0; y < rows; ++y) {
			for (int x = 0; x < columns; ++x) {
				accu += C(x) * C(y) * cof(x, x0) * cof(y, y0) * matrix[offset + x + (y * columns)];
			}
		}
		return (1f / 4f) * accu;
	}

	public double IDCT(int x0, int y0, byte[] matrix, int offset, int rows, int columns) {
		if (max < Math.max(rows, columns)) {
			throw new IndexOutOfBoundsException();
		}
		if ((x0 < 0) || (y0 < 0) || (max < x0) || (max < y0) || (rows <= 0) || (columns <= 0)) {
			throw new IndexOutOfBoundsException();
		}

		double accu = 0;
		for (int y = 0; y < rows; ++y) {
			for (int x = 0; x < columns; ++x) {
				accu += C(x) * C(y) * cof(x, x0) * cof(y, y0) * matrix[offset + x + (y * columns)];
			}
		}
		return (1f / 4f) * accu;
	}

}
