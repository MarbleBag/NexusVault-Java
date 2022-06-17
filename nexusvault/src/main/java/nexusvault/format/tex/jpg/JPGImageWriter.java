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

package nexusvault.format.tex.jpg;

import kreed.io.util.ByteAlignmentUtil;
import nexusvault.format.tex.Image;
import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.jpg.Context.Component;
import nexusvault.format.tex.jpg.tools.Constants;
import nexusvault.format.tex.jpg.tools.Constants.CompressionType;
import nexusvault.format.tex.jpg.tools.Constants.JPGType;
import nexusvault.format.tex.jpg.tools.Helper;
import nexusvault.format.tex.jpg.tools.MathHelper;
import nexusvault.format.tex.jpg.tools.Sampler;
import nexusvault.format.tex.jpg.tools.dct.FastDCT;
import nexusvault.format.tex.jpg.tools.huffman.BitConsumer;
import nexusvault.format.tex.jpg.tools.huffman.HuffmanEncoder;
import nexusvault.format.tex.util.ColorModelConverter;

public final class JPGImageWriter {
	private JPGImageWriter() {
	}

	public static byte[] compressSingleImage(TextureType target, Image image, int quality, int[] defaultColors) {
		if (quality < 0 || 100 < quality) {
			throw new IllegalArgumentException("quality");
		}

		final var jpgType = Helper.getJPGType(target);
		final var quantTables = Helper.adjustQuantTables(jpgType, quality);

		byte[] argb = null;
		switch (image.getFormat()) {
			case ARGB:
				argb = image.getData();
				break;
			case RGB:
				argb = ColorModelConverter.convertRGBToARGB(image.getData());
				break;
			case GRAYSCALE:
				argb = ColorModelConverter.convertGrayscaleToARGB(image.getData());
				break;
		}

		return compressARGB(jpgType, defaultColors, quantTables, argb, image.getWidth(), image.getHeight());
	}

	public static byte[] compressARGB(JPGType type, int[] defaultColors, float[][] quantTables, byte[] argb, int width, int height) {
		if (defaultColors.length != 4) {
			throw new IllegalArgumentException("defaultColor");
		}
		if (quantTables.length != 4) {
			throw new IllegalArgumentException("quantTables");
		}

		final var context = new Context(type, width, height);
		context.defaultValues = defaultColors;
		context.quantTables = quantTables;
		context.jpgOutput = new BitConsumer();

		switch (type) {
			case TYPE1:
				context.setComponentTypes(CompressionType.LUMINANCE, CompressionType.CHROMINANCE, CompressionType.CHROMINANCE, CompressionType.LUMINANCE);
				break;
			case TYPE2:
				context.setComponentTypes(CompressionType.LUMINANCE, CompressionType.LUMINANCE, CompressionType.LUMINANCE, CompressionType.LUMINANCE);
				break;
			case TYPE3:
				context.setComponentTypes(CompressionType.LUMINANCE, CompressionType.CHROMINANCE, CompressionType.CHROMINANCE, CompressionType.LUMINANCE);
				break;
		}

		switch (context.jpgType) {
			case TYPE1:
				encodeType1(context);
				break;
			case TYPE2:
			case TYPE3:
				encodeType2Or3(context);
				break;
		}

		context.jpgOutput.flush();
		final var size = context.jpgOutput.size();
		if (ByteAlignmentUtil.alignTo16Byte(size) != size) {
			final var missingBytes = ByteAlignmentUtil.alignTo16Byte(size) - size;
			for (var i = 0; i < missingBytes; ++i) {
				context.jpgOutput.consume(0xFF, Byte.SIZE);
			}
			context.jpgOutput.flush();
		}

		return context.jpgOutput.toByteArray();
	}

	private static void encodeType1(Context context) {
		var offset = 0;
		var column = 0;
		for (int i = 0; i < context.numberOfBlocks / 4; ++i) {
			if (column == context.blocksPerRow) {
				offset += context.componentsWidth * (Constants.BLOCK_HEIGHT * 2 - 1);
				column = 0;
			}

			final var offset1 = offset;
			final var offset2 = offset1 + Constants.BLOCK_WIDTH;
			final var offset3 = offset + context.componentsWidth * Constants.BLOCK_HEIGHT;
			final var offset4 = offset3 + Constants.BLOCK_WIDTH;

			decomposite(context, offset1);
			decomposite(context, offset2);
			decomposite(context, offset3);
			decomposite(context, offset4);

			if (context.defaultValues[0] <= -1) {
				final var component = context.components[0];
				compressSingleBlockA(context, component, offset1);
				compressSingleBlockA(context, component, offset2);
				compressSingleBlockA(context, component, offset3);
				compressSingleBlockA(context, component, offset4);
			}

			if (context.defaultValues[1] <= -1) {
				final var component = context.components[1];
				compressSingleBlockB(context, component, offset1);
			}

			if (context.defaultValues[2] <= -1) {
				final var component = context.components[2];
				compressSingleBlockB(context, component, offset1);
			}

			if (context.defaultValues[3] <= -1) {
				final var component = context.components[3];
				compressSingleBlockA(context, component, offset1);
				compressSingleBlockA(context, component, offset2);
				compressSingleBlockA(context, component, offset3);
				compressSingleBlockA(context, component, offset4);
			}

			offset += Constants.BLOCK_WIDTH * 2;
			column += 2;
		}
	}

	private static void encodeType2Or3(Context context) {
		var offset = 0;
		var column = 0;
		for (var i = 0; i < context.numberOfBlocks; ++i) {
			if (column == context.blocksPerRow) { // move write to next line of blocks
				offset += context.componentsWidth * (Constants.BLOCK_HEIGHT - 1);
				column = 0;
			}

			decomposite(context, offset);

			for (var c = 0; c < 4; ++c) {
				if (context.defaultValues[c] > -1) {
					continue;
				}

				final var component = context.components[c];
				compressSingleBlockA(context, component, offset);
			}

			offset += Constants.BLOCK_WIDTH;
			column += 1;
		}
	}

	private static void decomposite(Context context, int offset) {
		for (int y = 0; y < Constants.BLOCK_HEIGHT; ++y, offset += context.componentsWidth) {
			if (offset > context.image.length) {
				break;
			}
			for (int x = offset, p = offset * 4; x < offset + Constants.BLOCK_WIDTH; ++x, p += 4) {
				if (context.imageWidth <= x % context.componentsWidth) {
					break;
				}
				switch (context.jpgType) {
					case TYPE2:
						context.components[0].pixels[x] = context.image[p + 0] & 0xFF;
						context.components[1].pixels[x] = context.image[p + 1] & 0xFF;
						context.components[2].pixels[x] = context.image[p + 2] & 0xFF;
						context.components[3].pixels[x] = context.image[p + 3] & 0xFF;
						break;
					case TYPE1:
					case TYPE3:
						final int p4 = context.image[p + 0] & 0xFF;
						final int r4 = context.image[p + 1] & 0xFF;
						final int r2 = context.image[p + 2] & 0xFF;
						final int r3 = context.image[p + 3] & 0xFF;

						final int p2 = r4 - r3; // r4 = r3 + p2
						final int r1 = r3 + (p2 >> 1); // r3 = r1 - p2>>1
						final int p3 = r2 - r1; // r2 = r1 + p3
						final int p1 = r1 + (p3 >> 1); // r1 = p1 - p3>>1

						context.components[0].pixels[x] = MathHelper.clamp(p1, -256, 0xFF);
						context.components[1].pixels[x] = MathHelper.clamp(p2, -256, 0xFF);
						context.components[2].pixels[x] = MathHelper.clamp(p3, -256, 0xFF);
						context.components[3].pixels[x] = MathHelper.clamp(p4, 0, 0xFF);
						break;
				}
			}
		}
	}

	private static void compressSingleBlockA(Context context, Component component, int offset) {
		shiftAndClamp(context, component, offset);
		dct(context, component, offset);
		quantizate(context, component, offset);
		copyPixelBufferToBlock(context, component, offset);
		encodeBlock(context, component);
	}

	private static void compressSingleBlockB(Context context, Component component, int offset) {
		downscale(context, component, offset);
		shiftAndClamp(context, component, offset);
		dct(context, component, offset);
		quantizate(context, component, offset);
		copyPixelBufferToBlock(context, component, offset);
		encodeBlock(context, component);
	}

	private static void shiftAndClamp(Context context, Component component, int offset) {
		switch (component.compressionType) {
			case CHROMINANCE:
				shiftAndClamp(component.pixels, offset, context.componentsWidth, 0, -256, 255);
				break;
			case LUMINANCE:
				shiftAndClamp(component.pixels, offset, context.componentsWidth, -128, -256, 255);
				break;
		}
	}

	private static void shiftAndClamp(int[] data, int offset, int rowStride, int shift, int min, int max) {
		for (int y = 0; y < Constants.BLOCK_HEIGHT; ++y, offset += rowStride) {
			for (var x = offset; x < offset + Constants.BLOCK_WIDTH; ++x) {
				data[x] = Math.max(min, Math.min(max, data[x] + shift));
			}
		}
	}

	private static void downscale(Context context, Component component, int offset) {
		Sampler.downsample(component.pixels, offset, Constants.BLOCK_WIDTH * 2, Constants.BLOCK_HEIGHT * 2, context.componentsWidth, 2, component.pixels,
				offset, context.componentsWidth);
	}

	private static void dct(Context context, Component component, int offset) {
		FastDCT.dct(component.pixels, offset, context.componentsWidth);
	}

	private static void quantizate(Context context, Component component, int offset) {
		for (int y = 0, q = 0; y < Constants.BLOCK_HEIGHT; ++y) {
			for (var x = offset; x < offset + Constants.BLOCK_WIDTH; ++x) {
				component.pixels[x] = Math.round(component.pixels[x] / context.quantTables[component.index][q++]);
			}
			offset += context.componentsWidth;
		}
	}

	private static void copyPixelBufferToBlock(Context context, Component component, int offset) {
		for (int y = 0; y < Constants.BLOCK_HEIGHT; ++y) {
			final var start = offset + context.componentsWidth * y;
			System.arraycopy(component.pixels, start, context.block, Constants.BLOCK_WIDTH * y, Constants.BLOCK_WIDTH);
		}
	}

	private static void encodeBlock(Context context, Component component) {
		// zigzag
		for (int n = 0; n < Constants.BLOCK_SIZE; ++n) {
			context.uncompressed[Constants.ZIGZAG_SEQUENCE[n]] = context.block[n];
		}

		// adjust dc
		context.uncompressed[0] -= component.dc;
		component.dc = context.uncompressed[0];

		switch (component.compressionType) {
			case CHROMINANCE:
				HuffmanEncoder.encode(Constants.HUFFMAN_CHROMA_DC, Constants.HUFFMAN_CHROMA_AC, context.jpgOutput, context.uncompressed, 0,
						Constants.BLOCK_SIZE);
				break;
			case LUMINANCE:
				HuffmanEncoder.encode(Constants.HUFFMAN_LUMA_DC, Constants.HUFFMAN_LUMA_AC, context.jpgOutput, context.uncompressed, 0, Constants.BLOCK_SIZE);
				break;
		}
	}
}
