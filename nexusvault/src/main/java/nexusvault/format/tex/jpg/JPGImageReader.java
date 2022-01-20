package nexusvault.format.tex.jpg;

import nexusvault.format.tex.Image;
import nexusvault.format.tex.Image.ImageFormat;
import nexusvault.format.tex.jpg.Context.Component;
import nexusvault.format.tex.jpg.tools.Constants;
import nexusvault.format.tex.jpg.tools.Constants.CompressionType;
import nexusvault.format.tex.jpg.tools.Constants.JPGType;
import nexusvault.format.tex.jpg.tools.Helper;
import nexusvault.format.tex.jpg.tools.MathHelper;
import nexusvault.format.tex.jpg.tools.Sampler;
import nexusvault.format.tex.jpg.tools.dct.FastIntegerIDCT;
import nexusvault.format.tex.jpg.tools.huffman.BitSupply;
import nexusvault.format.tex.jpg.tools.huffman.HuffmanDecoder;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.VersionMismatchException;

public final class JPGImageReader {
	private JPGImageReader() {
	}

	public static Image decompress(StructFileHeader header, byte[] jpgImages, int offset, int mipMap) {
		if (header.signature != StructFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("tex", StructFileHeader.SIGNATURE, header.signature);
		}

		if (header.version != 3) {
			throw new VersionMismatchException("tex", 3, header.version);
		}

		final int length = header.mipmapSizes[mipMap];
		int arrayOffset = offset;
		for (int i = 0; i < mipMap; ++i) {
			arrayOffset += header.mipmapSizes[i];
		}

		int width = header.width >> header.mipMaps - mipMap - 1;
		int height = header.height >> header.mipMaps - mipMap - 1;
		width = width <= 0 ? 1 : width;
		height = height <= 0 ? 1 : height;

		final var jpgType = Helper.getJPGType(header);
		final var defaultColors = Helper.getDefaultColors(header);
		final var quantTables = Helper.getQuantTables(header);

		final var buffer = new byte[length];
		System.arraycopy(jpgImages, arrayOffset, buffer, 0, buffer.length);
		return decompress(jpgType, defaultColors, quantTables, buffer, width, height);
	}

	private static Image decompress(JPGType jpgType, int[] defaultColors, float[][] quantTables, byte[] jpg, int width, int height) {
		final var decodedImage = decompressToARGB(jpgType, defaultColors, quantTables, jpg, width, height);
		return new Image(width, height, ImageFormat.ARGB, decodedImage);
	}

	public static byte[] decompressToARGB(JPGType type, int[] defaultColors, float[][] quantTables, byte[] jpg, int width, int height) {
		if (defaultColors.length != 4) {
			throw new IllegalArgumentException("defaultColor");
		}
		if (quantTables.length != 4) {
			throw new IllegalArgumentException("quantTables");
		}

		final var context = new Context(type, width, height);
		context.defaultValues = defaultColors;
		context.quantTables = quantTables;
		context.jpgInput = new BitSupply(jpg);
		var totalWork = 0;
		switch (type) {
			case TYPE1:
				context.setComponentTypes(CompressionType.LUMINANCE, CompressionType.CHROMINANCE, CompressionType.CHROMINANCE, CompressionType.LUMINANCE);
				totalWork = (context.defaultValues[0] <= -1 ? context.numberOfBlocks : 0) //
						+ (context.defaultValues[1] <= -1 ? context.numberOfBlocks / 4 : 0) //
						+ (context.defaultValues[2] <= -1 ? context.numberOfBlocks / 4 : 0) //
						+ (context.defaultValues[3] <= -1 ? context.numberOfBlocks : 0);

				break;
			case TYPE2:
				context.setComponentTypes(CompressionType.LUMINANCE, CompressionType.LUMINANCE, CompressionType.LUMINANCE, CompressionType.LUMINANCE);
				totalWork = (context.defaultValues[0] <= -1 ? context.numberOfBlocks : 0) //
						+ (context.defaultValues[1] <= -1 ? context.numberOfBlocks : 0) //
						+ (context.defaultValues[2] <= -1 ? context.numberOfBlocks : 0) //
						+ (context.defaultValues[3] <= -1 ? context.numberOfBlocks : 0);
				break;
			case TYPE3:
				context.setComponentTypes(CompressionType.LUMINANCE, CompressionType.CHROMINANCE, CompressionType.CHROMINANCE, CompressionType.LUMINANCE);
				totalWork = (context.defaultValues[0] <= -1 ? context.numberOfBlocks : 0) //
						+ (context.defaultValues[1] <= -1 ? context.numberOfBlocks : 0) //
						+ (context.defaultValues[2] <= -1 ? context.numberOfBlocks : 0) //
						+ (context.defaultValues[3] <= -1 ? context.numberOfBlocks : 0);
				break;
		}

		for (var c = 0; c < 4; ++c) {
			if (context.defaultValues[c] > -1) {
				Helper.fillArray(context.components[c].pixels, context.defaultValues[c], 0, context.components[c].pixels.length);
			}
		}

		// context.latch = new CountDownLatch(totalWork);

		switch (context.jpgType) {
			case TYPE1:
				decodeType1(context);
				break;
			case TYPE2:
			case TYPE3:
				decodeType2Or3(context);
				break;
		}

		// try {
		// context.latch.await();
		// } catch (final InterruptedException e1) {
		// throw new TextureException(e1);
		// }
		//
		// context.latch = new CountDownLatch(context.numberOfBlocks);

		// for (int i = 0, offset = 0, column = 0; i < context.numberOfBlocks; ++i, ++column) {
		// if (column >= context.blocksPerRow) {
		// offset += context.componentsWidth * (Constants.BLOCK_HEIGHT - 1);
		// column = 0;
		// }
		// final var offset1 = offset;
		// context.executor.submit(() -> {
		// composite(context, offset1);
		// });
		// offset += Constants.BLOCK_WIDTH;
		//
		// }

		// try {
		// context.latch.await();
		// } catch (final InterruptedException e1) {
		// throw new TextureException(e1);
		// }

		return context.image;
	}

	private static void decodeType1(Context context) {
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

			if (context.defaultValues[0] <= -1) {
				final var component = context.components[0];
				decompressSingleBlockA(context, component, offset1);
				decompressSingleBlockA(context, component, offset2);
				decompressSingleBlockA(context, component, offset3);
				decompressSingleBlockA(context, component, offset4);
			}

			if (context.defaultValues[1] <= -1) {
				final var component = context.components[1];
				decompressSingleBlockB(context, component, offset1);
			}

			if (context.defaultValues[2] <= -1) {
				final var component = context.components[2];
				decompressSingleBlockB(context, component, offset1);
			}

			if (context.defaultValues[3] <= -1) {
				final var component = context.components[3];
				decompressSingleBlockA(context, component, offset1);
				decompressSingleBlockA(context, component, offset2);
				decompressSingleBlockA(context, component, offset3);
				decompressSingleBlockA(context, component, offset4);
			}

			composite(context, offset1);
			composite(context, offset2);
			composite(context, offset3);
			composite(context, offset4);

			offset += Constants.BLOCK_WIDTH * 2;
			column += 2;
		}
	}

	private static void decodeType2Or3(Context context) {
		var offset = 0;
		var column = 0;
		for (var i = 0; i < context.numberOfBlocks; ++i) {
			if (column == context.blocksPerRow) { // move write to next line of blocks
				offset += context.componentsWidth * (Constants.BLOCK_HEIGHT - 1);
				column = 0;
			}

			for (var c = 0; c < 4; ++c) {
				if (context.defaultValues[c] > -1) {
					continue;
				}

				final var component = context.components[c];
				decompressSingleBlockA(context, component, offset);
			}

			composite(context, offset);

			offset += Constants.BLOCK_WIDTH;
			column += 1;
		}

	}

	private static void decompressSingleBlockA(Context context, final Component component, final int offset) {
		decodeToBlock(context, component);
		copyBlockToPixelBuffer(context, component, offset);
		// context.executor.submit(() -> {
		// try {
		dequantizate(context, component, offset);
		idct(context, component, offset);
		shiftAndClamp(context, component, offset);
		// } finally {
		// context.latch.countDown();
		// }
		// });
	}

	private static void decompressSingleBlockB(Context context, final Component component, final int offset) {
		decodeToBlock(context, component);
		copyBlockToPixelBuffer(context, component, offset);
		// context.executor.submit(() -> {
		// try {
		dequantizate(context, component, offset);
		idct(context, component, offset);
		shiftAndClamp(context, component, offset);
		upscale(context, component, offset);
		// } finally {
		// context.latch.countDown();
		// }
		// });
	}

	private static void decodeToBlock(Context context, Component component) {
		switch (component.compressionType) {
			case CHROMINANCE:
				HuffmanDecoder.decode(Constants.HUFFMAN_CHROMA_DC, Constants.HUFFMAN_CHROMA_AC, context.jpgInput, context.uncompressed, 0,
						Constants.BLOCK_SIZE);
				break;
			case LUMINANCE:
				HuffmanDecoder.decode(Constants.HUFFMAN_LUMA_DC, Constants.HUFFMAN_LUMA_AC, context.jpgInput, context.uncompressed, 0, Constants.BLOCK_SIZE);
				break;
		}

		// adjust dc
		context.uncompressed[0] += component.dc;
		component.dc = context.uncompressed[0];

		// reverse zigzag
		for (int n = 0; n < Constants.BLOCK_SIZE; ++n) {
			context.block[n] = context.uncompressed[Constants.ZIGZAG_SEQUENCE[n]];
		}
	}

	private static void copyBlockToPixelBuffer(Context context, final Component component, int dstOffset) {
		for (int y = 0; y < Constants.BLOCK_HEIGHT; ++y) {
			final var dstStart = dstOffset + context.componentsWidth * y;
			System.arraycopy(context.block, Constants.BLOCK_WIDTH * y, component.pixels, dstStart, Constants.BLOCK_WIDTH);
		}
	}

	private static void dequantizate(Context context, Component component, int offset) {
		for (int y = 0, q = 0; y < Constants.BLOCK_HEIGHT; ++y) {
			for (var x = offset; x < offset + Constants.BLOCK_WIDTH; ++x) {
				component.pixels[x] *= context.quantTables[component.index][q++];
			}
			offset += context.componentsWidth;
		}
	}

	private static void idct(Context context, Component component, int offset) {
		FastIntegerIDCT.idct(component.pixels, offset, context.componentsWidth);
	}

	private static void shiftAndClamp(Context context, Component component, int offset) {
		switch (component.compressionType) {
			case CHROMINANCE:
				shiftAndClamp(component.pixels, offset, context.componentsWidth, 0, -256, 255);
				break;
			case LUMINANCE:
				shiftAndClamp(component.pixels, offset, context.componentsWidth, 128, 0, 255);
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

	private static void upscale(Context context, Component component, int offset) {
		Sampler.upsample(component.pixels, offset, Constants.BLOCK_WIDTH, Constants.BLOCK_HEIGHT, context.componentsWidth, 2, component.pixels, offset,
				context.componentsWidth);
	}

	private static void composite(Context context, int offset) {
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
						context.image[p + 0] = (byte) MathHelper.clamp(context.components[0].pixels[x], 0, 0xFF);
						context.image[p + 1] = (byte) MathHelper.clamp(context.components[1].pixels[x], 0, 0xFF);
						context.image[p + 2] = (byte) MathHelper.clamp(context.components[2].pixels[x], 0, 0xFF);
						context.image[p + 3] = (byte) MathHelper.clamp(context.components[3].pixels[x], 0, 0xFF);
						break;
					case TYPE1:
					case TYPE3:
						final int p1 = context.components[0].pixels[x];
						final int p2 = context.components[1].pixels[x];
						final int p3 = context.components[2].pixels[x];
						final int p4 = context.components[3].pixels[x];

						final int r1 = p1 - (p3 >> 1); // r1 = p1 - p3>>1
						final int r2 = MathHelper.clamp(r1 + p3, 0, 0xFF); // r2 = p1 - p3>>1 + p3
						final int r3 = MathHelper.clamp(r1 - (p2 >> 1), 0, 0xFF); // r3 = p1 - p3>>1 - p2>>1
						final int r4 = MathHelper.clamp(r3 + p2, 0, 0xFF); // r4 = p1 - p3>>1 - p2>>1 + p2

						context.image[p + 0] = (byte) MathHelper.clamp(p4, 0, 0xFF); // A = p4
						context.image[p + 1] = (byte) MathHelper.clamp(r4, 0, 0xFF); // R = p1 - p3/2 - p2/2 + p2
						context.image[p + 2] = (byte) MathHelper.clamp(r2, 0, 0xFF); // G = p1 - p3/2 + p3
						context.image[p + 3] = (byte) MathHelper.clamp(r3, 0, 0xFF); // B = p1 - p3/2 - p2/2
						break;
				}
			}
		}
		// context.latch.countDown();
	}

}
