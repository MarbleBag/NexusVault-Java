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
import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.util.ColorModelConverter;

public final class PlainImageWriter {
	private PlainImageWriter() {
	}

	public static byte[] getBinary(TextureType target, Image image) {
		switch (target) {
			case ARGB1:
			case ARGB2: {
				switch (image.getFormat()) {
					case ARGB:
						return ColorModelConverter.convertARGBToBGRA(image.getData());
					case RGB:
						return ColorModelConverter.convertRGBToBGRA(image.getData());
					case GRAYSCALE:
						return ColorModelConverter.convertGrayscaleToARGB(image.getData());
				}
				break;
			}
			case GRAYSCALE: {
				switch (image.getFormat()) {
					case ARGB:
						return ColorModelConverter.convertARGBToGrayscale(image.getData());
					case RGB:
						return ColorModelConverter.convertRGBToGrayscale(image.getData());
					case GRAYSCALE:
						return image.getData();
				}
				break;
			}
			case RGB: {
				switch (image.getFormat()) {
					case ARGB:
						return ColorModelConverter.packARGBToB5G6B5(image.getData());
					case RGB:
						return ColorModelConverter.packRGBToB5G6B5(image.getData());
					case GRAYSCALE:
						return ColorModelConverter.packGrayscaleToB5G6B5(image.getData());
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid texture type: " + target);
		}

		throw new IllegalArgumentException(String.format("Unable to write image with format %s as %s", image.getFormat(), target));
	}
}
