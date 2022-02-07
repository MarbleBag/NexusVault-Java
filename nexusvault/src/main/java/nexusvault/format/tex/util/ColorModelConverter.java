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

package nexusvault.format.tex.util;

public final class ColorModelConverter {

	public static byte[] packRGBToBGR565(byte[] src) {
		final byte[] dst = new byte[src.length / 3 * 2];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 2) {
			final var r = src[s + 0]; // R
			final var g = src[s + 1]; // G
			final var b = src[s + 2]; // B
			final int data = (r & 0xF8) << 8 | (g & 0xFC) << 3 | (b & 0xF8) << 0;
			dst[d + 0] = (byte) (data >> 0 & 0xFF);
			dst[d + 1] = (byte) (data >> 8 & 0xFF);
		}
		return dst;
	}

	public static byte[] packARGBToBGR565(byte[] src) {
		final byte[] dst = new byte[src.length / 2];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 2) {
			final var r = src[s + 1]; // R
			final var g = src[s + 2]; // G
			final var b = src[s + 3]; // B
			final int data = (r & 0xF8) << 8 | (g & 0xFC) << 3 | (b & 0xF8) << 0;
			dst[d + 1] = (byte) (data >> 8 & 0xFF);
			dst[d + 0] = (byte) (data >> 0 & 0xFF);
		}
		return dst;
	}

	public static byte[] packGrayscaleToBGR565(byte[] src) {
		final byte[] dst = new byte[src.length * 2];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 2) {
			final var shade = src[s + 0]; // R
			final int data = (shade & 0xF8) << 8 | (shade & 0xFC) << 3 | (shade & 0xF8) << 0;
			dst[d + 1] = (byte) (data >> 8 & 0xFF);
			dst[d + 0] = (byte) (data >> 0 & 0xFF);
		}
		return dst;
	}

	public static byte[] unpackBGR565ToRGB(byte[] src) {
		final byte[] dst = new byte[src.length / 2 * 3];
		for (int d = 0, s = 0; d < dst.length; d += 3, s += 2) {
			dst[d++] = (byte) (src[s + 1] & 0x1F); // r
			dst[d++] = (byte) (src[s + 0] << 3 | src[s + 1] >>> 5); // g
			dst[d++] = (byte) (src[s + 0] >>> 3); // b
		}
		return dst;
	}

	public static byte[] convertRGBToBGR(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int i = 0; i < dst.length; i += 3) {
			dst[i + 2] = src[i + 0];
			dst[i + 1] = src[i + 1];
			dst[i + 0] = src[i + 2];
		}
		return dst;
	}

	public static byte[] convertBGRToRGB(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int i = 0; i < dst.length; i += 3) {
			dst[i + 2] = src[i + 0];
			dst[i + 1] = src[i + 1];
			dst[i + 0] = src[i + 2];
		}
		return dst;
	}

	public static byte[] convertBGRToARGB(byte[] src) {
		final byte[] dst = new byte[src.length / 3 * 4];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 4) {
			dst[d + 0] = (byte) 0xFF;
			dst[d + 3] = src[s + 0];
			dst[d + 2] = src[s + 1];
			dst[d + 1] = src[s + 2];
		}
		return dst;
	}

	public static byte[] convertRGBToBGRA(byte[] src) {
		final byte[] dst = new byte[src.length / 3 * 4];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 4) {
			dst[d + 3] = (byte) 0xFF;
			dst[d + 2] = src[s + 0];
			dst[d + 1] = src[s + 1];
			dst[d + 0] = src[s + 2];
		}
		return dst;
	}

	public static byte[] convertARGBToABGR(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int i = 0; i < dst.length; i += 4) {
			dst[i + 0] = src[i + 0];
			dst[i + 3] = src[i + 1];
			dst[i + 2] = src[i + 2];
			dst[i + 1] = src[i + 3];
		}
		return dst;
	}

	public static byte[] convertARGBToBGRA(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int i = 0; i < dst.length; i += 4) {
			dst[i + 3] = src[i + 0];
			dst[i + 2] = src[i + 1];
			dst[i + 1] = src[i + 2];
			dst[i + 0] = src[i + 3];
		}
		return dst;
	}

	public static byte[] convertABGRToARGB(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int i = 0; i < dst.length; i += 4) {
			dst[i + 0] = src[i + 0];
			dst[i + 3] = src[i + 1];
			dst[i + 2] = src[i + 2];
			dst[i + 1] = src[i + 3];
		}
		return dst;
	}

	public static void inplaceConvertARGBToRGBA(byte[] arr) {
		for (int i = 0; i < arr.length; i += 4) {
			final var a = arr[i + 0];
			final var r = arr[i + 1];
			final var g = arr[i + 2];
			final var b = arr[i + 3];
			arr[i + 0] = r;
			arr[i + 1] = g;
			arr[i + 2] = b;
			arr[i + 3] = a;
		}
	}

	public static void inplaceConvertRGBAToARGB(byte[] arr) {
		for (int i = 0; i < arr.length; i += 4) {
			final var r = arr[i + 0];
			final var g = arr[i + 1];
			final var b = arr[i + 2];
			final var a = arr[i + 3];
			arr[i + 0] = a;
			arr[i + 1] = r;
			arr[i + 2] = g;
			arr[i + 3] = b;
		}
	}

	public static void inplaceConvertBGRAToARGB(byte[] arr) {
		for (int i = 0; i < arr.length; i += 4) {
			final var b = arr[i + 0];
			final var g = arr[i + 1];
			final var r = arr[i + 2];
			final var a = arr[i + 3];
			arr[i + 0] = a;
			arr[i + 1] = r;
			arr[i + 2] = g;
			arr[i + 3] = b;
		}
	}

	public static byte[] convertARGBToRGBA(byte[] src) {
		final byte[] dst = new byte[src.length];
		for (int i = 0; i < dst.length; i += 4) {
			dst[i + 3] = src[i + 0];
			dst[i + 0] = src[i + 1];
			dst[i + 1] = src[i + 2];
			dst[i + 2] = src[i + 3];
		}
		return dst;
	}

	public static byte[] convertGrayscaleToRGBA(byte[] src) {
		final byte[] dst = new byte[src.length * 4];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 4) {
			dst[d + 0] = src[s + 0];
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
			dst[d + 3] = (byte) 0xFF;
		}
		return dst;
	}

	public static byte[] convertARGBToGrayscale(byte[] src) {
		final byte[] dst = new byte[src.length / 4];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 1) {
			final var r = src[s + 1];
			final var g = src[s + 2];
			final var b = src[s + 3];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			dst[d] = (byte) luminosity;
		}
		return dst;
	}

	public static byte[] convertABGRToGrayscale(byte[] src) {
		final byte[] dst = new byte[src.length / 4];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 1) {
			final var b = src[s + 1];
			final var g = src[s + 2];
			final var r = src[s + 3];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			dst[d] = (byte) luminosity;
		}
		return dst;
	}

	public static byte[] convertGrayscaleToARGB(byte[] src) {
		final byte[] dst = new byte[src.length * 4];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 4) {
			dst[d + 0] = (byte) 0xFF;
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
			dst[d + 3] = src[s + 0];
		}
		return dst;
	}

	public static byte[] convertGrayscaleToBGRA(byte[] src) {
		final byte[] dst = new byte[src.length * 4];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 4) {
			dst[d + 0] = src[s + 0];
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
			dst[d + 3] = (byte) 0xFF;
		}
		return dst;
	}

	public static byte[] convertARGBToRGB(byte[] src) {
		final byte[] dst = new byte[src.length / 4 * 3];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 3) {
			dst[d + 0] = src[s + 1];
			dst[d + 1] = src[s + 2];
			dst[d + 2] = src[s + 3];
		}
		return dst;
	}

	public static byte[] convertABGRToRGB(byte[] src) {
		final byte[] dst = new byte[src.length / 4 * 3];
		for (int s = 0, d = 0; d < dst.length; s += 4, d += 3) {
			dst[d + 2] = src[s + 1];
			dst[d + 1] = src[s + 2];
			dst[d + 0] = src[s + 3];
		}
		return dst;
	}

	public static byte[] convertRGBToARGB(byte[] src) {
		final byte[] dst = new byte[src.length / 3 * 4];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 4) {
			dst[d + 0] = (byte) 0xFF;
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 1];
			dst[d + 3] = src[s + 2];
		}
		return dst;
	}

	public static byte[] convertRGBToRGBA(byte[] src) {
		final byte[] dst = new byte[src.length / 3 * 4];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 4) {
			dst[d + 0] = src[s + 0];
			dst[d + 1] = src[s + 1];
			dst[d + 2] = src[s + 2];
			dst[d + 3] = (byte) 0xFF;
		}
		return dst;
	}

	public static byte[] convertGrayscaleToRGB(byte[] src) {
		final byte[] dst = new byte[src.length * 3];
		for (int s = 0, d = 0; d < dst.length; s += 1, d += 3) {
			dst[d + 0] = src[s + 0];
			dst[d + 1] = src[s + 0];
			dst[d + 2] = src[s + 0];
		}
		return dst;
	}

	public static byte[] convertRGBToGrayscale(byte[] src) {
		final byte[] dst = new byte[src.length / 3];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 1) {
			final var r = src[s + 0];
			final var g = src[s + 1];
			final var b = src[s + 2];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			dst[d] = (byte) luminosity;
		}
		return dst;
	}

	public static byte[] convertBGRToGrayscale(byte[] src) {
		final byte[] dst = new byte[src.length / 3];
		for (int s = 0, d = 0; d < dst.length; s += 3, d += 1) {
			final var b = src[s + 0];
			final var g = src[s + 1];
			final var r = src[s + 2];
			final var luminosity = Math.min(255, Math.max(0, Math.round(0.21f * r + 0.72f * g + 0.07f * b)));
			dst[d] = (byte) luminosity;
		}
		return dst;
	}

}