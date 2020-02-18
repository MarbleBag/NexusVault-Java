package nexusvault.format.tex.jpg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import nexusvault.format.tex.AbstractTextureImageWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureImageWriter;
import nexusvault.format.tex.jpg.tool.encoder.JPGEncoder;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class JPGTextureImageWriter extends AbstractTextureImageWriter implements TextureImageWriter {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.JPEG_TYPE_1, TexType.JPEG_TYPE_2, TexType.JPEG_TYPE_3));

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
		header.imageSizesCount = images.length;

		final var encoder = new JPGEncoder(target, new boolean[] { false, false, false, false }, new int[] { 100, 100, 100, 100 }); // TODO

		final var imageData = new ByteBuffer[images.length];
		var paddedImageDataSize = 0;
		for (var i = 0; i < images.length; ++i) {
			final var image = images[i];
			final var encodedImageData = encoder.encode(image.getImageData(), image.getImageWidth(), image.getImageHeight());
			final var paddedLength = encodedImageData.remaining() + 15 & 0xFFFFFFFFFFFFF0L;
			header.imageSizes[i] = (int) paddedLength;
			paddedImageDataSize += header.imageSizes[i];
			imageData[i] = encodedImageData;
		}

		final var output = ByteBuffer.allocate(StructTextureFileHeader.SIZE_IN_BYTES + paddedImageDataSize).order(ByteOrder.LITTLE_ENDIAN);
		kreed.reflection.struct.StructUtil.writeStruct(header, output, true);
		for (var i = 0; i < images.length; ++i) {
			final var remainder = header.imageSizes[i] - imageData[i].remaining();
			output.put(imageData[i]);
			for (var j = 0; j < remainder; ++j) {
				output.put((byte) 0xFF);
			}
		}

		output.flip();
		return output;
	}

	private void assertTexType(TexType target) {
		switch (target) {
			case JPEG_TYPE_1:
			case JPEG_TYPE_2:
			case JPEG_TYPE_3:
				return;
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	protected void assertImageData(TextureImage[] images) {
		super.assertImageData(images);

		for (int i = images.length - 1; 0 <= i; --i) {
			if (images[i].getImageFormat() != TextureImageFormat.ARGB) {
				throw new TextureJPGEncodingException(String.format("Expected image format %s. TextureImageFormat of image %d was %s. ",
						TextureImageFormat.ARGB, i, TextureImageFormat.ARGB));
			}
		}
	}

}
