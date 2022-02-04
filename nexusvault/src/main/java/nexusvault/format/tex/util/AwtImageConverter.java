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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

import nexusvault.format.tex.Image;
import nexusvault.format.tex.Image.ImageFormat;
import nexusvault.format.tex.TextureException;

public final class AwtImageConverter {
	private AwtImageConverter() {
	}

	public static BufferedImage convertToBufferedImage(Image image) {
		BufferedImage result = null;
		byte[] resultData = null;

		switch (image.getFormat()) {
			case GRAYSCALE:
				result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
				resultData = new byte[image.getData().length];
				System.arraycopy(image.getData(), 0, resultData, 0, resultData.length);
				break;

			case RGB:
				result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				resultData = ColorModelConverter.convertRGBToBGR(image.getData());
				break;

			case ARGB:
				result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
				resultData = ColorModelConverter.convertARGBToABGR(image.getData());
				break;

		}

		result.setData(Raster.createRaster(result.getSampleModel(), new DataBufferByte(resultData, resultData.length), null));
		return result;
	}

	/**
	 * Converts a {@link BufferedImage} to {@link Image} with the given {@link ImageFormat}.
	 *
	 * @param format
	 *            the {@link ImageFormat}
	 * @param bufferedImage
	 *            Image which can be converted
	 * @return a {@link Image} created from the given image
	 * @throws TextureException
	 *             if the image is not convertible
	 */
	public static Image convertToTextureImage(ImageFormat format, BufferedImage bufferedImage) {
		byte[] imageData = null;

		switch (bufferedImage.getType()) {
			case BufferedImage.TYPE_4BYTE_ABGR:
			case BufferedImage.TYPE_3BYTE_BGR:
			case BufferedImage.TYPE_BYTE_GRAY:
				break; // good
			default:
				if (bufferedImage.getColorModel().hasAlpha()) {
					bufferedImage = convert(bufferedImage, BufferedImage.TYPE_4BYTE_ABGR);
				} else {
					bufferedImage = convert(bufferedImage, BufferedImage.TYPE_3BYTE_BGR);
				}
				break;
		}

		final var bufferedImageData = extractImageData(bufferedImage);

		switch (format) {
			case ARGB:
				switch (bufferedImage.getType()) {
					case BufferedImage.TYPE_4BYTE_ABGR:
						imageData = ColorModelConverter.convertABGRToARGB(bufferedImageData);
						break;
					case BufferedImage.TYPE_3BYTE_BGR:
						imageData = ColorModelConverter.convertBGRToARGB(bufferedImageData);
						break;
					case BufferedImage.TYPE_BYTE_GRAY:
						imageData = ColorModelConverter.convertGrayscaleToARGB(bufferedImageData);
						break;
				}
				break;

			case RGB:
				switch (bufferedImage.getType()) {
					case BufferedImage.TYPE_4BYTE_ABGR:
						imageData = ColorModelConverter.convertABGRToRGB(bufferedImageData);
						break;
					case BufferedImage.TYPE_3BYTE_BGR:
						imageData = ColorModelConverter.convertBGRToRGB(bufferedImageData);
						break;
					case BufferedImage.TYPE_BYTE_GRAY:
						imageData = ColorModelConverter.convertGrayscaleToRGB(bufferedImageData);
						break;
				}
				break;

			case GRAYSCALE:
				switch (bufferedImage.getType()) {
					case BufferedImage.TYPE_4BYTE_ABGR:
						imageData = ColorModelConverter.convertABGRToGrayscale(bufferedImageData);
						break;
					case BufferedImage.TYPE_3BYTE_BGR:
						imageData = ColorModelConverter.convertBGRToGrayscale(bufferedImageData);
						break;
					case BufferedImage.TYPE_BYTE_GRAY:
						imageData = bufferedImageData;
						break;
				}
				break;
		}

		if (imageData == null) {
			throw new TextureException(String.format("BufferedImage type '%s' to texture format '%s' not supported", bufferedImage.getType(), format));
		}
		return new Image(bufferedImage.getWidth(), bufferedImage.getHeight(), format, imageData);
	}

	private static BufferedImage convert(BufferedImage image, int newType) {
		if (image.getType() == newType) {
			return image;
		}
		final var newImage = new BufferedImage(image.getWidth(), image.getHeight(), newType);
		final var g2d = newImage.createGraphics();
		g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		g2d.dispose();
		return newImage;
	}

	private static byte[] extractImageData(BufferedImage image) {
		final DataBuffer dataBuffer = image.getData().getDataBuffer();
		if (!(dataBuffer instanceof DataBufferByte)) {
			throw new TextureException(String.format("BufferedImage DataBuffer type %s not supported", dataBuffer.getClass()));
		}
		return ((DataBufferByte) dataBuffer).getData();
	}

}