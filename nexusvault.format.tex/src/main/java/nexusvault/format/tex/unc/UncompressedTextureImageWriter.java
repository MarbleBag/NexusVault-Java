package nexusvault.format.tex.unc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import nexusvault.format.tex.AbstractTextureImageWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageWriter;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class UncompressedTextureImageWriter extends AbstractTextureImageWriter implements TextureImageWriter {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.ARGB_1, TexType.ARGB_2, TexType.RGB, TexType.GRAYSCALE));

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return this.acceptedTypes;
	}

	@Override
	public ByteBuffer writeTexture(TexType target, TextureImage[] images) {
		assertTexType(target);
		assertImageOrder(images);
		assertImageData(images);

		final var header = new StructTextureFileHeader(true);
		header.width = images[images.length - 1].getImageWidth();
		header.height = images[images.length - 1].getImageHeight();
		// header.depth = ?;
		// header.sides = ?;
		header.mipMaps = images.length;
		header.format = target.getFormat();
		header.isCompressed = target.isCompressed();
		header.compressionFormat = target.getCompressionFormat();
		// header.imageSizesCount = images.length; //Not used for uncompressed textures

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

	private void assertTexType(TexType target) {
		switch (target) {
			case ARGB_1:
			case ARGB_2:
			case RGB:
			case GRAYSCALE:
				return;
			default:
				throw new IllegalArgumentException();
		}
	}

	private UncompressedImageEncoder getEncoder(TexType target) {
		switch (target) {
			case ARGB_1:
			case ARGB_2:
				return new ARGB8888ImageEncoder();
			case RGB:
				return new RGB565ImageEncoder();
			case GRAYSCALE:
				return new Gray8ImageEncoder();
			default:
				throw new IllegalArgumentException();
		}
	}

}
