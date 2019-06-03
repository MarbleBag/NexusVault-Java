package nexusvault.format.tex.jpg;

final class ZigZagPatternBuilder {
	private ZigZagPatternBuilder() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Be i row index and j column index of a mXn matrix and 0&lt=i&ltm and 0&lt=j&ltn. The returned array <b>R</b> is one dimensional of length m*n and
	 * contains the resulting series of indices if a mXn matrix is transformed by the zigzag pattern. Especially, to find the ij-th element in <b>R</b> the
	 * index p = i + j*n can be calculated. Therefore <b>R[p]</b> is the index of the ij-th element if stored in a zigzag pattern.
	 * <p>
	 * <b>Example</b><br>
	 * Be M a 3x3 matrix of the form <b><br>
	 * [a b c]<br>
	 * [d e f]<br>
	 * [g h k]<br>
	 * </b> and the letters a to k denotes to their corresponding index (i,j) within M (a=(0,0); b=(0,1); c=(0,2); d=(1,0);...) Written to an one dimensional
	 * array, column first, results in Array <b>A [a,d,g,b,e,h,c,f,k]</b>. The position of each element in <b>A</b> can be calculated with <i>i + j*n</i>. The
	 * zigzag pattern applied on M results in array <b>B [a,b,d,g,e,c,f,h,k]</b>. The returned array of this function contains the information at which index an
	 * element of <b>A</b> can be found in <b>B</b> and vice versa. In this case the returned array <b>R</b> is <b>[0,2,3,1,4,7,5,6,8]</b>, therefore it is
	 * <b>A[u] = B[R[u]]</b>
	 */
	public static int[] calculateColumnFirstZigZagIndices(final int rows, final int columns) {
		final int[] indices = new int[rows * columns];
		int direction = 1;
		for (int n = 0, i = 0, j = 0; n < (rows * columns); ++n) {
			indices[i + (j * columns)] = n;
			i -= direction;
			j += direction;

			if (direction > 0) {
				if (j >= columns) { // oob
					j = columns - 1;
					i = i + 2;
					direction = -direction;
				} else if (i < 0) { // oob
					i = 0;
					direction = -direction;
				}
			} else {
				if (i >= rows) { // oob
					i = rows - 1;
					j = j + 2;
					direction = -direction;
				} else if (j < 0) { // oob
					j = 0;
					direction = -direction;
				}
			}
		}
		return indices;
	}

	/**
	 * Function to calculate the indices mapping for a row first matrix. See the column first {@link #calculateColumnFirstZigZagIndices(int, int) function} for
	 * more information.
	 *
	 * @see #calculateColumnFirstZigZagIndices(int, int)
	 * @param rows
	 * @param columns
	 * @return
	 */
	public static int[] calculateRowFirstZigZagIndices(final int rows, final int columns) {
		final int[] indices = new int[rows * columns];
		int direction = 1;
		for (int n = 0, i = 0, j = 0; n < (rows * columns); ++n) {
			indices[i + (j * rows)] = n;
			i += direction;
			j -= direction;

			if (direction > 0) {
				if (i >= rows) { // oob
					i = rows - 1;
					j = j + 2;
					direction = -direction;
				} else if (j < 0) { // oob
					j = 0;
					direction = -direction;
				}
			} else {
				if (j >= columns) { // oob
					j = columns - 1;
					i = i + 2;
					direction = -direction;
				} else if (i < 0) { // oob
					i = 0;
					direction = -direction;
				}
			}
		}
		return indices;
	}

	public static int[] applyColumnFirstZigZag(final int[] matrix, final int rows, final int columns) {
		final int[] result = new int[rows * columns];
		final int[] pattern = calculateColumnFirstZigZagIndices(rows, columns);
		applyZigZag(matrix, result, pattern);
		return result;
	}

	public static int[] applyColumnFirstInverseZigZag(final int[] zigzag, final int rows, final int columns) {
		final int[] result = new int[rows * columns];
		final int[] pattern = calculateColumnFirstZigZagIndices(rows, columns);
		applyInverseZigZag(zigzag, result, pattern);
		return result;
	}

	public static int[] applyRowFirstZigZag(final int[] matrix, final int rows, final int columns) {
		final int[] result = new int[rows * columns];
		final int[] pattern = calculateRowFirstZigZagIndices(rows, columns);
		applyZigZag(matrix, result, pattern);
		return result;
	}

	public static int[] applyRowFirstInverseZigZag(final int[] zigzag, final int rows, final int columns) {
		final int[] result = new int[rows * columns];
		final int[] pattern = calculateRowFirstZigZagIndices(rows, columns);
		applyInverseZigZag(zigzag, result, pattern);
		return result;
	}

	private static void applyZigZag(final int[] matrix, final int[] result, final int[] pattern) {
		for (int p = 0; p < pattern.length; ++p) {
			result[pattern[p]] = matrix[p];
		}
	}

	private static void applyInverseZigZag(final int[] zigzag, final int[] result, final int[] pattern) {
		for (int p = 0; p < pattern.length; ++p) {
			result[p] = zigzag[pattern[p]];
		}
	}

}
