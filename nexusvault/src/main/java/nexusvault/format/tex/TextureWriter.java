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

package nexusvault.format.tex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import kreed.io.util.ByteArrayBinaryWriter;
import nexusvault.format.tex.dxt.DXTImageWriter;
import nexusvault.format.tex.jpg.JPGImageWriter;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.format.tex.uncompressed.PlainImageWriter;
import nexusvault.format.tex.util.TextureMipMapGenerator;

public final class TextureWriter {
	private TextureWriter() {

	}

	public static byte[] toBinary(TextureType target, Image image, int mipmapCount, int quality, int[] defaultColors) {
		final var mipmaps = generateMipMaps(image, mipmapCount);
		return toBinary(target, mipmaps, quality, defaultColors);
	}

	public static void write(TextureType target, Image image, int mipmapCount, int quality, int[] defaultColors, SeekableByteChannel out) throws IOException {
		final var binary = toBinary(target, image, mipmapCount, quality, defaultColors);
		final var buffer = ByteBuffer.wrap(binary);
		out.write(buffer);
	}

	private static byte[] toBinary(TextureType target, Image[] mipmaps, int quality, int[] defaultColors) {
		final var header = new StructFileHeader();
		header.width = mipmaps[mipmaps.length - 1].getWidth();
		header.height = mipmaps[mipmaps.length - 1].getHeight();
		header.version = 3;
		header.depth = 1;
		header.sides = 1;
		header.textureCount = mipmaps.length;
		header.format = target.getFormat();
		header.isJpg = target.isJpg();
		header.jpgFormat = target.getJpgFormat();
		header.imageSizesCount = mipmaps.length;

		sortImagesSmallToLarge(mipmaps);

		var fileSize = StructFileHeader.SIZE_IN_BYTES;
		final var encodesImages = new byte[mipmaps.length][];
		for (int i = 0; i < mipmaps.length; i++) {
			encodesImages[i] = compressImage(target, mipmaps[i], quality, defaultColors);
			header.imageSizes[i] = encodesImages[i].length;
			fileSize += encodesImages[i].length;
		}

		final var writer = new ByteArrayBinaryWriter(new byte[fileSize], ByteOrder.LITTLE_ENDIAN);
		header.write(writer);
		for (final byte[] encodesImage : encodesImages) {
			writer.writeInt8(encodesImage, 0, encodesImage.length);
		}
		return writer.getDecoratedObject();
	}

	private static byte[] compressImage(TextureType target, Image image, int quality, int[] defaultColors) {
		switch (target) {
			case DXT1:
			case DXT3:
			case DXT5:
				return DXTImageWriter.compressSingleImage(target, image);
			case JPG1:
			case JPG2:
			case JPG3:
				return JPGImageWriter.compressSingleImage(target, image, quality, defaultColors);
			case ARGB1:
			case ARGB2:
			case RGB:
			case GRAYSCALE:
				return PlainImageWriter.getBinary(target, image);
			default:
				throw new IllegalArgumentException("Invalid texture type: " + target);
		}
	}

	public static Image[] generateMipMaps(Image image, int mipmaps) {
		return TextureMipMapGenerator.generate(image, mipmaps);
	}

	public static Image[] sortImagesSmallToLarge(Image[] mipmaps) {
		final var copy = new Image[mipmaps.length];
		System.arraycopy(mipmaps, 0, copy, 0, copy.length);
		Arrays.sort(copy, (a, b) -> {
			final int order = a.getHeight() * a.getWidth() - b.getHeight() * b.getWidth();
			return order; // smallest to largest
		});
		return copy;
	}

}
