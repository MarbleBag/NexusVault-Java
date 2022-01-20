package nexusvault.format.tex;

import java.nio.ByteOrder;
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

	public static byte[] writeBinary(TextureType target, Image image, int mipmapCount, int quality, int[] defaultColors) {
		final var mipmaps = generateMipMaps(image, mipmapCount);
		return writeBinary(target, mipmaps, quality, defaultColors);
	}

	private static byte[] writeBinary(TextureType target, Image[] mipmaps, int quality, int[] defaultColors) {
		final var header = new StructFileHeader();
		header.width = mipmaps[mipmaps.length - 1].getWidth();
		header.height = mipmaps[mipmaps.length - 1].getHeight();
		header.version = 3;
		header.depth = 1;
		header.sides = 1;
		header.mipMaps = mipmaps.length;
		header.format = target.getFormat();
		header.isJpg = target.isJpg();
		header.jpgFormat = target.getJpgFormat();
		header.mipmapSizesCount = mipmaps.length;

		sortImagesSmallToLarge(mipmaps);

		var fileSize = StructFileHeader.SIZE_IN_BYTES;
		final var encodesImages = new byte[mipmaps.length][];
		for (int i = 0; i < mipmaps.length; i++) {
			encodesImages[i] = compressImage(target, mipmaps[i], quality, defaultColors);
			header.mipmapSizes[i] = encodesImages[i].length;
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
		return TextureMipMapGenerator.buildMipMaps(image, mipmaps);
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
