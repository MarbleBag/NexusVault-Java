package nexusvault.format.tex.dxt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ddsutil.DDSUtil;
import gr.zdimensions.jsquish.Squish;
import nexusvault.format.tex.AbstractTextureImageWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageWriter;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class DXTTextureImageWriter extends AbstractTextureImageWriter implements TextureImageWriter {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.DXT1, TexType.DXT3, TexType.DXT5));

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
		// header.imageSizesCount = images.length; //Not used for dxt textures

		final var dxtCompression = getCompressionType(target);
		final var imageData = new ByteBuffer[images.length];
		var imageSizes = 0;

		for (var i = 0; i < images.length; ++i) {
			final var image = images[i];
			final var bufferedImage = image.convertToBufferedImage();
			final var compressed = DDSUtil.compressTexture(bufferedImage, dxtCompression);
			// header.imageSizes[i] = encodedImageData.length; //Not used for dxt textures
			imageSizes += compressed.remaining();
			imageData[i] = compressed;
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
			case DXT1:
			case DXT3:
			case DXT5:
				return;
			default:
				throw new IllegalArgumentException();
		}
	}

	private Squish.CompressionType getCompressionType(TexType target) {
		switch (target) {
			case DXT1:
				return Squish.CompressionType.DXT1;
			case DXT3:
				return Squish.CompressionType.DXT3;
			case DXT5:
				return Squish.CompressionType.DXT5;
			default:
				throw new IllegalArgumentException(/* TODO */);
		}
	}

}
