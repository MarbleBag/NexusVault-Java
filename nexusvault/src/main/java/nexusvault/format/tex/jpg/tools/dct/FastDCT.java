/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.tex.jpg.tools.dct;

public final class FastDCT {
	private FastDCT() {
	}

	private static final double[] coefficients = new double[64];
	private static final int N = 8;

	private static final double CO_SCALE = N + N;
	private static final double DCT_SCALE = 1d / 4d;

	static {
		for (var k = 0; k < N; ++k) {
			for (var n = 0; n < N; ++n) {
				coefficients[n + k * N] = Math.cos(Math.PI * (2 * n + 1) * k / CO_SCALE);
			}
		}
	}

	/**
	 *
	 * @param data
	 *            row-major matrix 8x8
	 * @param offset
	 *            matrix start
	 * @param rowStride
	 *            distance between the start of two rows
	 *
	 */
	public static void dct(int[] data, int offset, int rowStride) {
		final var buffer = new int[N * N];

		// TODO this can still be optimized

		for (var k1 = 0; k1 < N; ++k1) {
			for (var k2 = 0; k2 < N; ++k2) {
				double sum = 0;
				for (var y2 = 0; y2 < N; ++y2) {
					for (var x2 = 0; x2 < N; ++x2) {
						sum += data[y2 * rowStride + x2 + offset] * coefficients[y2 + k1 * N] * coefficients[x2 + k2 * N];
					}
				}
				buffer[k1 * N + k2] = (int) Math.round(DCT_SCALE * sum);
			}
		}

		for (var i = 0; i < N; ++i) {
			System.arraycopy(buffer, i * N, data, offset + i * rowStride, N);
		}
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
