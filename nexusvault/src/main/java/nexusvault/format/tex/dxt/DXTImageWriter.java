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
import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.util.ColorModelConverter;

public final class DXTImageWriter {
	private DXTImageWriter() {
	}

	public static byte[] compressSingleImage(TextureType dxtType, Image image) {
		byte[] rgba = null;

		switch (image.getFormat()) {
			case ARGB:
				rgba = ColorModelConverter.convertARGBToRGBA(image.getData());
				break;
			case RGB:
				rgba = ColorModelConverter.convertRGBToRGBA(image.getData());
				break;
			case GRAYSCALE:
				rgba = ColorModelConverter.convertGrayscaleToRGBA(image.getData());
				break;
		}

		return compressSingleImage(dxtType, rgba, image.getWidth(), image.getHeight());
	}

	public static byte[] compressSingleImage(TextureType dxtType, byte[] rgba, int width, int height) {
		final var compressionType = getCompressionType(dxtType);
		final int storageRequirements = Squish.getStorageRequirements(width, height, compressionType);
		final var compressedImage = Squish.compressImage(rgba, width, height, new byte[storageRequirements], compressionType,
				Squish.CompressionMethod.CLUSTER_FIT);
		return compressedImage;
	}

	private static Squish.CompressionType getCompressionType(TextureType type) {
		switch (type) {
			case DXT1:
				return Squish.CompressionType.DXT1;
			case DXT3:
				return Squish.CompressionType.DXT3;
			case DXT5:
				return Squish.CompressionType.DXT5;
			default:
				throw new IllegalArgumentException("Invalid texture type: " + type);
		}
	}

}
