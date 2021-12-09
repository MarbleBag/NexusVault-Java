package nexusvault.format.tex.unc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import nexusvault.format.tex.AbstractTextureImageWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageWriter;
import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Thread-Safe
 */
public final class UncompressedTextureImageWriter extends AbstractTextureImageWriter implements TextureImageWriter {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.ARGB1, TexType.ARGB2, TexType.RGB, TexType.GRAYSCALE));

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return this.acceptedTypes;
	}

	@Override
	public ByteBuffer writeTextureAfterValidation(TexType target, TextureImage[] images, Map<String, Object> config) {
		final var header = new StructTextureFileHeader(true);
		header.width = images[images.length - 1].getImageWidth();
		header.height = images[images.length - 1].getImageHeight();
		header.depth = (int) config.getOrDefault(CONFIG_DEPTH, 1);
		header.sides = (int) config.getOrDefault(CONFIG_SIDES, 1);
		header.mipMaps = images.length;
		header.format = target.getFormat();
		header.isJpg = target.isJpg();
		header.jpgFormat = target.getJpgFormat();
		header.mipmapSizesCount = 0; // Not used for uncompressed textures

		final var encoder = getEncoder(target);
		final var imageData = new byte[images.length][];
		var imageSizes = 0;

		for (var i = 0; i < images.length; ++i) {
			final var image = images[i];
			final var encodedImageData = encoder.encode(image.getImageData(), image.getImageWidth(), image.getImageHeight(), image.getImageFormat());
			// header.imageSizes[i] = encodedImageData.length; //Not used for uncompressed textures
			imageSizes += encodedImageData.length;
			imageData[i] = encodedImageData;
		}

		final var output = ByteBuffer.allocate(StructTextureFileHeader.SIZE_IN_BYTES + imageSizes).order(ByteOrder.LITTLE_ENDIAN);
		kreed.reflection.struct.StructUtil.writeStruct(header, output, true);

		for (final var image : imageData) {
			output.put(image);
		}

		output.flip();
		return output;
	}

	private UncompressedImageEncoder getEncoder(TexType target) {
		switch (target) {
			case ARGB1:
			case ARGB2:
				return new ARGB8888ImageEncoder();
			case RGB:
				return new RGB565ImageEncoder();
			case GRAYSCALE:
				return new Gray8ImageEncoder();
			default:
				throw new IllegalArgumentException(String.format("TexType %s is not supported by this writer", target));
		}
	}

}
