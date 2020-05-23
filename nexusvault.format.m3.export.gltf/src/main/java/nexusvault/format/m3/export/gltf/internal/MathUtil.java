package nexusvault.format.m3.export.gltf.internal;

/*
 * import org.la4j.LinearAlgebra.InverterFactory; import org.la4j.Matrix;
 */

public final class MathUtil {
	private MathUtil() {

	}

	public static float[] transpose(float[] matrix) {
		return new float[] { //
				matrix[0], matrix[4], matrix[8], matrix[12], //
				matrix[1], matrix[5], matrix[9], matrix[13], //
				matrix[2], matrix[6], matrix[10], matrix[14], //
				matrix[3], matrix[7], matrix[11], matrix[15] //
		};
	}

	public static double[] toDouble(float[] matrix) {
		final var r = new double[matrix.length];
		for (var i = 0; i < r.length; ++i) {
			r[i] = matrix[i];
		}
		return r;
	}

	public static float[] toFloat(double[] matrix) {
		final var r = new float[matrix.length];
		for (var i = 0; i < r.length; ++i) {
			r[i] = (float) matrix[i];
		}
		return r;
	}

	/*
	 * private static double[] toArray(Matrix matrix) { return new double[] { // matrix.get(0, 0), matrix.get(1, 0), matrix.get(2, 0), matrix.get(3, 0), //
	 * matrix.get(0, 1), matrix.get(1, 1), matrix.get(2, 1), matrix.get(3, 1), // matrix.get(0, 2), matrix.get(1, 2), matrix.get(2, 2), matrix.get(3, 2), //
	 * matrix.get(0, 3), matrix.get(1, 3), matrix.get(2, 3), matrix.get(3, 3) // }; }
	 * 
	 * public static float[] inverse(float[] array) { final var matrix = org.la4j.Matrix.from1DArray(4, 4, toDouble(transpose(array))); final var invMatrix =
	 * matrix.withInverter(InverterFactory.GAUSS_JORDAN).inverse(); return toFloat(toArray(invMatrix)); }
	 */

	private static float m(float[] matrix, int i, int j) {
		return matrix[i * 4 + j];
	}

	public static float[] toQuaternion(float[] m) {
		double t;
		double[] q;

		if (m(m, 2, 2) < 0) {
			if (m(m, 0, 0) > m(m, 1, 1)) {
				t = 1 + m(m, 0, 0) - m(m, 1, 1) - m(m, 2, 2);
				q = new double[] { t, m(m, 0, 1) + m(m, 1, 0), m(m, 2, 0) + m(m, 0, 2), m(m, 1, 2) - m(m, 2, 1) };
			} else {
				t = 1 - m(m, 0, 0) + m(m, 1, 1) - m(m, 2, 2);
				q = new double[] { m(m, 0, 1) + m(m, 1, 0), t, m(m, 1, 2) + m(m, 2, 1), m(m, 2, 0) - m(m, 0, 2) };
			}
		} else {
			if (m(m, 0, 0) < -m(m, 1, 1)) {
				t = 1 - m(m, 0, 0) - m(m, 1, 1) + m(m, 2, 2);
				q = new double[] { m(m, 2, 0) + m(m, 0, 2), m(m, 1, 2) + m(m, 2, 1), t, m(m, 0, 1) - m(m, 1, 0) };
			} else {
				t = 1 + m(m, 0, 0) + m(m, 1, 1) + m(m, 2, 2);
				q = new double[] { m(m, 1, 2) - m(m, 2, 1), m(m, 2, 0) - m(m, 0, 2), m(m, 0, 1) - m(m, 1, 0), t };
			}
		}

		final var p = 0.5 / Math.sqrt(t);
		q[0] *= p;
		q[1] *= p;
		q[2] *= p;
		q[3] *= p;
		final var r = new float[] { (float) q[0], (float) q[1], (float) q[2], (float) q[3] };
		return r;
	}

}
