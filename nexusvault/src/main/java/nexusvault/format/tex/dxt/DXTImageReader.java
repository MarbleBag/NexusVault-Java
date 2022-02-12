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

package nexusvault.format.tex.dxt;

import com.github.goldsam.jsquish.Squish;

import nexusvault.format.tex.Image;
import nexusvault.format.tex.Image.ImageFormat;
import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.format.tex.util.ColorModelConverter;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class DXTImageReader {
	private DXTImageReader() {

	}

	public static Image decompress(StructFileHeader header, byte[] dxtImages, int offset, int mipMap) {
		if (header.signature != StructFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("tex", StructFileHeader.SIGNATURE, header.signature);
		}

		if (header.version != 3) {
			throw new VersionMismatchException("tex", 3, header.version);
		}

		final var textureType = TextureType.resolve(header);
		final var compressionType = getCompressionType(textureType);

		int width = 0, height = 0, length = 0, arrayOffset = offset;
		for (int i = 0; i <= mipMap; ++i) {
			width = (int) (header.width / Math.pow(2, header.textureCount - 1 - i));
			height = (int) (header.height / Math.pow(2, header.textureCount - 1 - i));
			width = width <= 0 ? 1 : width;
			height = height <= 0 ? 1 : height;
			arrayOffset += length;
			length = (width + 3) / 4 * ((height + 3) / 4) * compressionType.blockSize;
		}

		final var buffer = new byte[length];
		System.arraycopy(dxtImages, arrayOffset, buffer, 0, buffer.length);
		return decompressSingleImage(textureType, buffer, width, height);
	}

	public static Image decompressSingleImage(TextureType type, byte[] dxtData, int width, int height) {
		final var decompressedImage = decompressToRGBA(type, dxtData, width, height);
		ColorModelConverter.inplaceConvertRGBAToARGB(decompressedImage);
		return new Image(width, height, ImageFormat.ARGB, decompressedImage);
	}

	public static byte[] decompressToRGBA(TextureType dxtType, byte[] dxt, int width, int height) {
		return Squish.decompressImage(null, width, height, dxt, getCompressionType(dxtType));
	}

	private static Squish.CompressionType getCompressionType(TextureType dxtType) {
		switch (dxtType) {
			case DXT1:
				return Squish.CompressionType.DXT1;
			case DXT3:
				return Squish.CompressionType.DXT3;
			case DXT5:
				return Squish.CompressionType.DXT5;
			default:
				throw new IllegalArgumentException("Invalid texture type: " + dxtType);
		}
	}
}
