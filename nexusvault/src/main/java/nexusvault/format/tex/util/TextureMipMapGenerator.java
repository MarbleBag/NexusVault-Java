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

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import nexusvault.format.tex.Image;

public final class TextureMipMapGenerator {
	private TextureMipMapGenerator() {
	}

	/**
	 * Generates a mipmap for a given image. Images are stored in sequence in an array. The first image is the original image.
	 * 
	 * @param src
	 *            The original image
	 * @param numberOfMipMaps
	 *            Number of mipmaps, needs to be at least 1
	 * @return A Mipmap
	 */
	public static Image[] generate(Image src, int numberOfMipMaps) {
		if (numberOfMipMaps == 0) {
			throw new IllegalArgumentException("Argument: 'numberofMipMaps' must not be 0");
		}

		if (numberOfMipMaps == 1) {
			return new Image[] { src };
		}

		if (numberOfMipMaps < 0) {
			final var value = Math.min(src.getWidth(), src.getHeight());
			numberOfMipMaps = (int) Math.ceil(Math.log(value) / Math.log(2));
		}

		final var tmp = new BufferedImage[numberOfMipMaps];
		tmp[0] = AwtImageConverter.convertToBufferedImage(src);
		for (int i = 1; i < numberOfMipMaps; ++i) {
			tmp[i] = scaleDown(tmp[i - 1]);
		}

		final var images = new Image[numberOfMipMaps];
		for (int i = 0; i < images.length; ++i) {
			images[i] = AwtImageConverter.convertToTextureImage(src.getFormat(), tmp[i]);
		}

		return images;
	}

	private static BufferedImage scaleDown(BufferedImage input) {
		final var newWidth = Math.max(1, input.getWidth() >> 1);
		final var newHeight = Math.max(1, input.getHeight() >> 1);

		final var output = new BufferedImage(newWidth, newHeight, input.getType());
		final var g2d = output.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage(input, 0, 0, newWidth, newHeight, null);
		g2d.dispose();

		return output;
	}

}
