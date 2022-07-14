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
import nexusvault.format.tex.Image.ImageFormat;

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
			numberOfMipMaps = (int) Math.floor(Math.log(value) / Math.log(2)) + 1;
		}
		// TODO: implement bicubic or lanczos interpolation
		switch (src.getFormat()) {
			case ARGB:
				return generateWithAlpha(src, numberOfMipMaps);
			case GRAYSCALE:
			case RGB:
			default:
				return generateWithoutAlpha(src, numberOfMipMaps);
		}
	}

	private static Image[] generateWithAlpha(Image src, int numberOfMipMaps) {
		final var tmpDiffuse = new BufferedImage[numberOfMipMaps];
		final var tmpAlpha = new BufferedImage[numberOfMipMaps];

		{
			final var srcData = src.getData();
			final var diffuseData = new byte[srcData.length / 4 * 3];
			final var alphaData = new byte[srcData.length / 4];
			for (int s = 0, d = 0, a = 0; s < srcData.length; s += 4, d += 3, a += 1) {
				alphaData[a + 0] = srcData[s + 0];
				diffuseData[d + 0] = srcData[s + 1];
				diffuseData[d + 1] = srcData[s + 2];
				diffuseData[d + 2] = srcData[s + 3];
			}

			tmpDiffuse[0] = AwtImageConverter.convertToBufferedImage(new Image(src.getWidth(), src.getHeight(), ImageFormat.RGB, diffuseData));
			tmpAlpha[0] = AwtImageConverter.convertToBufferedImage(new Image(src.getWidth(), src.getHeight(), ImageFormat.GRAYSCALE, alphaData));
		}

		for (int i = 1; i < numberOfMipMaps; ++i) {
			tmpDiffuse[i] = scaleDown(tmpDiffuse[i - 1]);
			tmpAlpha[i] = scaleDown(tmpAlpha[i - 1]);
		}

		final var images = new Image[numberOfMipMaps];
		images[0] = src;

		for (int i = 1; i < images.length; ++i) {
			final var diffuseImage = AwtImageConverter.convertToTextureImage(ImageFormat.RGB, tmpDiffuse[i]);
			final var alphaImage = AwtImageConverter.convertToTextureImage(ImageFormat.GRAYSCALE, tmpAlpha[i]);

			final var diffuseData = diffuseImage.getData();
			final var alphaData = alphaImage.getData();
			final var imageData = new byte[diffuseData.length + alphaData.length];

			for (int s = 0, d = 0, a = 0; s < imageData.length; s += 4, d += 3, a += 1) {
				imageData[s + 0] = alphaData[a + 0];
				imageData[s + 1] = diffuseData[d + 0];
				imageData[s + 2] = diffuseData[d + 1];
				imageData[s + 3] = diffuseData[d + 2];
			}

			images[i] = new Image(diffuseImage.getWidth(), diffuseImage.getHeight(), ImageFormat.ARGB, imageData);
		}

		return images;
	}

	private static Image[] generateWithoutAlpha(Image src, int numberOfMipMaps) {
		final var tmp = new BufferedImage[numberOfMipMaps];
		tmp[0] = AwtImageConverter.convertToBufferedImage(src);
		for (int i = 1; i < numberOfMipMaps; ++i) {
			tmp[i] = scaleDown(tmp[i - 1]);
		}

		final var images = new Image[numberOfMipMaps];
		images[0] = src;
		for (int i = 1; i < images.length; ++i) {
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
