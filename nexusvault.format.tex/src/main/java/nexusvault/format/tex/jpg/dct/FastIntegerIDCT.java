package nexusvault.format.tex.jpg.dct;

/* idct.c, inverse fast discrete cosine transform */

/* Copyright (C) 1996, MPEG Software Simulation Group. All Rights Reserved. */

/*
 * Disclaimer of Warranty
 *
 * These software programs are available to the user without any license fee or royalty on an "as is" basis. The MPEG Software Simulation Group disclaims any
 * and all warranties, whether express, implied, or statuary, including any implied warranties or merchantability or of fitness for a particular purpose. In no
 * event shall the copyright-holder be liable for any incidental, punitive, or consequential damages of any kind whatsoever arising from the use of these
 * programs.
 *
 * This disclaimer of warranty extends to the user of these programs and user's customers, employees, agents, transferees, successors, and assigns.
 *
 * The MPEG Software Simulation Group does not represent or warrant that the programs furnished hereunder are free of infringement of any third-party patents.
 *
 * Commercial implementations of MPEG-1 and MPEG-2 video, including shareware, are subject to royalty fees to patent holders. Many of these patents are general
 * enough such that they are unavoidable regardless of implementation design.
 *
 */

/**********************************************************/
/* inverse two dimensional DCT, Chen-Wang algorithm */
/* (cf. IEEE ASSP-32, pp. 803-816, Aug. 1984) */
/* 32-bit integer arithmetic (8 bit coefficients) */
/* 11 mults, 29 adds per DCT */
/* sE, 18.8.91 */
/**********************************************************/
/* coefficients extended to 12 bit for IEEE1180-1990 */
/* compliance sE, 2.1.94 */
/**********************************************************/

public final class FastIntegerIDCT {

	private final static int W1 = 2841; /* 2048*sqrt(2)*cos(1*pi/16) */
	private final static int W2 = 2676; /* 2048*sqrt(2)*cos(2*pi/16) */
	private final static int W3 = 2408; /* 2048*sqrt(2)*cos(3*pi/16) */
	private final static int W5 = 1609; /* 2048*sqrt(2)*cos(5*pi/16) */
	private final static int W6 = 1108; /* 2048*sqrt(2)*cos(6*pi/16) */
	private final static int W7 = 565; /* 2048*sqrt(2)*cos(7*pi/16) */

	private final static int[] iclp = new int[1024];
	private final static int iclp_offset = 512;

	static {
		for (int i = -512; i < 512; i++) {
			iclp[iclp_offset + i] = (short) (i < -256 ? -256 : i > 255 ? 255 : i);
		}
	}

	/**
	 *
	 * @param data
	 *            row-major matrix 8x8
	 * @param dataOffset
	 *            matrix start
	 */
	public static void idct(int[] data, int dataOffset) {
		for (int i = 0; i < 8; ++i) {
			idctRow(data, dataOffset + 8 * i);
		}

		for (int i = 0; i < 8; ++i) {
			idctColumn(data, dataOffset + i);
		}
	}

	private static void idctRow(int[] data, int i) {
		int x0, x1, x2, x3, x4, x5, x6, x7, x8;
		x1 = data[i + 4] << 11;
		x2 = data[i + 6];
		x3 = data[i + 2];
		x4 = data[i + 1];
		x5 = data[i + 7];
		x6 = data[i + 5];
		x7 = data[i + 3];
		final boolean useShortCut = (x1 | x2 | x3 | x4 | x5 | x6 | x7) == 0;
		if (useShortCut) {
			data[i + 0] = data[i + 1] = data[i + 2] = data[i + 3] = data[i + 4] = data[i + 5] = data[i + 6] = data[i + 7] = data[i + 0] << 3;
			return;
		}

		x0 = (data[i + 0] << 11) + 128;

		/* first stage */
		x8 = W7 * (x4 + x5);
		x4 = x8 + (W1 - W7) * x4;
		x5 = x8 - (W1 + W7) * x5;
		x8 = W3 * (x6 + x7);
		x6 = x8 - (W3 - W5) * x6;
		x7 = x8 - (W3 + W5) * x7;

		/* second stage */
		x8 = x0 + x1;
		x0 -= x1;
		x1 = W6 * (x3 + x2);
		x2 = x1 - (W2 + W6) * x2;
		x3 = x1 + (W2 - W6) * x3;
		x1 = x4 + x6;
		x4 -= x6;
		x6 = x5 + x7;
		x5 -= x7;

		/* third stage */
		x7 = x8 + x3;
		x8 -= x3;
		x3 = x0 + x2;
		x0 -= x2;
		x2 = 181 * (x4 + x5) + 128 >> 8;
		x4 = 181 * (x4 - x5) + 128 >> 8;

		/* fourth stage */
		data[i + 0] = x7 + x1 >> 8;
		data[i + 1] = x3 + x2 >> 8;
		data[i + 2] = x0 + x4 >> 8;
		data[i + 3] = x8 + x6 >> 8;
		data[i + 4] = x8 - x6 >> 8;
		data[i + 5] = x0 - x4 >> 8;
		data[i + 6] = x3 - x2 >> 8;
		data[i + 7] = x7 - x1 >> 8;

	}

	private static void idctColumn(int[] data, int i) {
		int x0, x1, x2, x3, x4, x5, x6, x7, x8;
		x1 = data[i + 8 * 4] << 8;
		x2 = data[i + 8 * 6];
		x3 = data[i + 8 * 2];
		x4 = data[i + 8 * 1];
		x5 = data[i + 8 * 7];
		x6 = data[i + 8 * 5];
		x7 = data[i + 8 * 3];
		final boolean useShortCut = (x1 | x2 | x3 | x4 | x5 | x6 | x7) == 0;
		if (useShortCut) {
			data[i + 8 * 0] = data[i + 8 * 1] = data[i + 8 * 2] = data[i
					+ 8 * 3] = data[i + 8 * 4] = data[i + 8 * 5] = data[i + 8 * 6] = data[i + 8 * 7] = iclp[iclp_offset + (data[i + 8 * 0] + 32 >> 6)];
			return;
		}

		x0 = (data[i + 8 * 0] << 8) + 8192;

		/* first stage */
		x8 = W7 * (x4 + x5) + 4;
		x4 = x8 + (W1 - W7) * x4 >> 3;
		x5 = x8 - (W1 + W7) * x5 >> 3;
		x8 = W3 * (x6 + x7) + 4;
		x6 = x8 - (W3 - W5) * x6 >> 3;
		x7 = x8 - (W3 + W5) * x7 >> 3;

		/* second stage */
		x8 = x0 + x1;
		x0 -= x1;
		x1 = W6 * (x3 + x2) + 4;
		x2 = x1 - (W2 + W6) * x2 >> 3;
		x3 = x1 + (W2 - W6) * x3 >> 3;
		x1 = x4 + x6;
		x4 -= x6;
		x6 = x5 + x7;
		x5 -= x7;

		/* third stage */
		x7 = x8 + x3;
		x8 -= x3;
		x3 = x0 + x2;
		x0 -= x2;
		x2 = 181 * (x4 + x5) + 128 >> 8;
		x4 = 181 * (x4 - x5) + 128 >> 8;

		/* fourth stage */
		data[i + 8 * 0] = iclp[iclp_offset + (x7 + x1 >> 14)];
		data[i + 8 * 1] = iclp[iclp_offset + (x3 + x2 >> 14)];
		data[i + 8 * 2] = iclp[iclp_offset + (x0 + x4 >> 14)];
		data[i + 8 * 3] = iclp[iclp_offset + (x8 + x6 >> 14)];
		data[i + 8 * 4] = iclp[iclp_offset + (x8 - x6 >> 14)];
		data[i + 8 * 5] = iclp[iclp_offset + (x0 - x4 >> 14)];
		data[i + 8 * 6] = iclp[iclp_offset + (x3 - x2 >> 14)];
		data[i + 8 * 7] = iclp[iclp_offset + (x7 - x1 >> 14)];
	}

}
