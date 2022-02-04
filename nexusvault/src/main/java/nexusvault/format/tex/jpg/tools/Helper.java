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

package nexusvault.format.tex.jpg.tools;

import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.jpg.tools.Constants.CompressionType;
import nexusvault.format.tex.jpg.tools.Constants.JPGType;
import nexusvault.format.tex.struct.StructFileHeader;

public final class Helper {
	private Helper() {
	}

	public static int[] getQuantTable(CompressionType type) {
		switch (type) {
			case CHROMINANCE:
				return Constants.QUANT_TABLE_CHROMA;
			case LUMINANCE:
				return Constants.QUANT_TABLE_LUMA;
			default:
				throw new IllegalArgumentException();
		}
	}

	public static float[] adjustQuantTable(int[] quantTable, int qualityFactor) {
		final float scale = (200 - qualityFactor * 2) * 0.01f;
		final float[] adjustedQuantTable = new float[quantTable.length];
		for (int i = 0; i < adjustedQuantTable.length; ++i) {
			adjustedQuantTable[i] = Math.max(1f, Math.min(quantTable[i] * scale, 255f));
		}
		return adjustedQuantTable;
	}

	public static float[] adjustQuantTable(CompressionType type, int qualityFactor) {
		return adjustQuantTable(getQuantTable(type), qualityFactor);
	}

	public static float[][] adjustQuantTables(JPGType type, int qualityFactor) {
		return adjustQuantTables(type, qualityFactor, qualityFactor, qualityFactor, qualityFactor);
	}

	public static float[][] adjustQuantTables(JPGType type, int qualityFactor1, int qualityFactor2, int qualityFactor3, int qualityFactor4) {
		switch (type) {
			case TYPE1:
			case TYPE3:
				return new float[][] { //
						adjustQuantTable(CompressionType.LUMINANCE, qualityFactor1), //
						adjustQuantTable(CompressionType.CHROMINANCE, qualityFactor2), //
						adjustQuantTable(CompressionType.CHROMINANCE, qualityFactor3), //
						adjustQuantTable(CompressionType.LUMINANCE, qualityFactor4) //
				};
			case TYPE2:
				return new float[][] { //
						adjustQuantTable(CompressionType.LUMINANCE, qualityFactor1), //
						adjustQuantTable(CompressionType.LUMINANCE, qualityFactor2), //
						adjustQuantTable(CompressionType.LUMINANCE, qualityFactor3), //
						adjustQuantTable(CompressionType.LUMINANCE, qualityFactor4) //
				};
		}
		throw new IllegalArgumentException("Invalid jpg type: " + type);
	}

	public static float[][] getQuantTables(StructFileHeader header) {
		final var type = getJPGType(header);
		return adjustQuantTables(type, header.jpgChannelInfos[0].quality, header.jpgChannelInfos[1].quality, header.jpgChannelInfos[2].quality,
				header.jpgChannelInfos[3].quality);
	}

	public static int[] getDefaultColors(StructFileHeader header) {
		return new int[] { getDefaultColor(header, 0), getDefaultColor(header, 1), getDefaultColor(header, 2), getDefaultColor(header, 3) };
	}

	private static int getDefaultColor(StructFileHeader header, int idx) {
		return header.jpgChannelInfos[idx].hasColor ? header.jpgChannelInfos[idx].color & 0xFF : -1;
	}

	public static JPGType getJPGType(StructFileHeader header) {
		return getJPGType(TextureType.resolve(header));
	}

	public static JPGType getJPGType(TextureType type) {
		switch (type) {
			case JPG1:
				return JPGType.TYPE1;
			case JPG2:
				return JPGType.TYPE2;
			case JPG3:
				return JPGType.TYPE3;
			default:
				throw new IllegalArgumentException("Invalid texture type: " + type);
		}
	}

	public static void fillArray(int[] pixels, int defaultColors, int offset, int length) {
		final var startIdx = offset;
		final var endIdx = startIdx + length;
		pixels[startIdx] = defaultColors;
		for (int dstPos = startIdx + 1, copyLength = 1; dstPos < endIdx; dstPos += copyLength, copyLength += copyLength) {
			System.arraycopy(pixels, startIdx, pixels, dstPos, dstPos + copyLength > endIdx ? endIdx - dstPos : copyLength);
		}
	}

}
