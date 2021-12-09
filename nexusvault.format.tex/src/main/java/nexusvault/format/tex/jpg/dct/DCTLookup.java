package nexusvault.format.tex.jpg.dct;

public final class DCTLookup {

	private static final double Cx_sqr = 1 / Math.sqrt(2);

	private final double[] lookup;
	private final int max;

	public DCTLookup(int rows, int columns) {
		this.max = Math.max(rows, columns);
		this.lookup = new double[this.max * this.max];
		for (int x0 = 0; x0 < this.max; x0++) {
			for (int x = 0; x < this.max; x++) {
				this.lookup[x + x0 * this.max] = Math.cos((2f * x0 + 1f) * x * Math.PI / 16f);
			}
		}
	}

	public double cof(int x, int x0) {
		return this.lookup[x + x0 * this.max];
	}

	public double C(int n) {
		return n == 0 ? Cx_sqr : 1;
	}

	public double DCT(int x0, int y0, int[] matrix, int offset, int rows, int columns) {
		if (this.max < Math.max(rows, columns)) {
			throw new IndexOutOfBoundsException();
		}
		if (x0 < 0 || y0 < 0 || this.max < x0 || this.max < y0 || rows <= 0 || columns <= 0) {
			throw new IndexOutOfBoundsException();
		}

		double accu = 0;
		for (int y = 0; y < rows; ++y) {
			for (int x = 0; x < columns; ++x) {
				accu += cof(x, x0) * cof(y, y0) * matrix[offset + x + y * columns];
			}
		}
		return 1f / 4f * C(x0) * C(y0) * accu;
	}

	public double DCT(int x0, int y0, short[] matrix, int offset, int rows, int columns) {
		if (this.max < Math.max(rows, columns)) {
			throw new IndexOutOfBoundsException();
		}
		if (x0 < 0 || y0 < 0 || this.max < x0 || this.max < y0 || rows <= 0 || columns <= 0) {
			throw new IndexOutOfBoundsException();
		}

		double accu = 0;
		for (int y = 0; y < rows; ++y) {
			for (int x = 0; x < columns; ++x) {
				accu += cof(x, x0) * cof(y, y0) * matrix[offset + x + y * columns];
			}
		}
		return 1f / 4f * C(x0) * C(y0) * accu;
	}

	public double DCT(int x0, int y0, byte[] matrix, int offset, int rows, int columns) {
		if (this.max < Math.max(rows, columns)) {
			throw new IndexOutOfBoundsException();
		}
		if (x0 < 0 || y0 < 0 || this.max < x0 || this.max < y0 || rows <= 0 || columns <= 0) {
			throw new IndexOutOfBoundsException();
		}

		double accu = 0;
		for (int y = 0; y < rows; ++y) {
			for (int x = 0; x < columns; ++x) {
				accu += cof(x, x0) * cof(y, y0) * matrix[offset + x + y * columns];
			}
		}
		return 1f / 4f * C(x0) * C(y0) * accu;
	}

}
