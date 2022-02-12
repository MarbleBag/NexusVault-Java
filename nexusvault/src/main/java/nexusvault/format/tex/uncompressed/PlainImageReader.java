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

package nexusvault.format.tex.uncompressed;

import nexusvault.format.tex.Image;
import nexusvault.format.tex.Image.ImageFormat;
import nexusvault.format.tex.TextureException;
import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.format.tex.util.ColorModelConverter;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class PlainImageReader {
	private PlainImageReader() {
	}

	public static Image getImage(StructFileHeader header, byte[] images, int offset, int mipMap) {
		if (header.signature != StructFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("tex", StructFileHeader.SIGNATURE, header.signature);
		}

		if (header.version != 3) {
			throw new VersionMismatchException("tex", 3, header.version);
		}

		final var textureType = TextureType.resolve(header);

		int bytesPerPixel = 1;
		switch (textureType) {
			case ARGB1:
			case ARGB2:
				bytesPerPixel = 4;
				break;
			case GRAYSCALE:
				bytesPerPixel = 1;
				break;
			case RGB:
				bytesPerPixel = 2;
				break;
			default:
				throw new IllegalArgumentException("Invalid texture type: " + textureType);
		}

		int width = 0, height = 0, length = 0, arrayOffset = offset;
		for (int i = 0; i <= mipMap; ++i) {
			width = (int) (header.width / Math.pow(2, header.textureCount - 1 - i));
			height = (int) (header.height / Math.pow(2, header.textureCount - 1 - i));
			width = width <= 0 ? 1 : width;
			height = height <= 0 ? 1 : height;
			arrayOffset += length;
			length = width * height * bytesPerPixel;
		}

		final var buffer = new byte[length];
		System.arraycopy(images, arrayOffset, buffer, 0, buffer.length);

		switch (textureType) {
			case ARGB1:
			case ARGB2:
				ColorModelConverter.inplaceConvertBGRAToARGB(buffer);
				return new Image(width, height, ImageFormat.ARGB, buffer);
			case RGB:
				return new Image(width, height, ImageFormat.RGB, ColorModelConverter.unpackBGR565ToRGB(buffer));
			case GRAYSCALE:
				return new Image(width, height, ImageFormat.GRAYSCALE, buffer);
			default:
				throw new TextureException();
		}
	}

}
