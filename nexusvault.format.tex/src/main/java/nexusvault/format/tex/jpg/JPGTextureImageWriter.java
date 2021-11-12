package nexusvault.format.tex.jpg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import kreed.io.util.ByteAlignmentUtil;
import nexusvault.format.tex.AbstractTextureImageWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureEncodingException;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureImageWriter;
import nexusvault.format.tex.jpg.tool.encoder.JPGEncoder;
import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Thread-Safe
 *
 * Configuration:
 * <ul>
 * <li>{@link #CONFIG_QUALITY}
 * <li>{@link #CONFIG_QUALITY_LAYER1}
 * <li>{@link #CONFIG_QUALITY_LAYER2}
 * <li>{@link #CONFIG_QUALITY_LAYER3}
 * <li>{@link #CONFIG_QUALITY_LAYER4}
 * <li>{@link #CONFIG_VALUE_LAYER1}
 * <li>{@link #CONFIG_VALUE_LAYER2}
 * <li>{@link #CONFIG_VALUE_LAYER3}
 * <li>{@link #CONFIG_VALUE_LAYER4}
 * </ul>
 */
public final class JPGTextureImageWriter extends AbstractTextureImageWriter implements TextureImageWriter {

	/**
	 * jpg quality, the lower, the higher the compression will be. This will be used on all layers. The default is 100. Needs to be a natural number from [0,
	 * 100].
	 */
	public static final String CONFIG_QUALITY = "jpg.quality";
	/**
	 * jpg quality, the lower, the higher the compression will be. Needs to be a natural number from [0, 100].
	 */
	public static final String CONFIG_QUALITY_LAYER1 = "jpg.quality.l1";
	/**
	 * jpg quality, the lower, the higher the compression will be. Needs to be a natural number from [0, 100].
	 */
	public static final String CONFIG_QUALITY_LAYER2 = "jpg.quality.l2";
	/**
	 * jpg quality, the lower, the higher the compression will be. Needs to be a natural number from [0, 100].
	 */
	public static final String CONFIG_QUALITY_LAYER3 = "jpg.quality.l3";
	/**
	 * jpg quality, the lower, the higher the compression will be. Needs to be a natural number from [0, 100].
	 */
	public static final String CONFIG_QUALITY_LAYER4 = "jpg.quality.l4";
	/**
	 * If given, the image channel associated with this layer will not be written, instead this value (byte or int from [0, 255]) will be used for the whole
	 * layer!
	 */
	public static final String CONFIG_VALUE_LAYER1 = "jpg.value.l1";
	/**
	 * If given, the image channel associated with this layer will not be written, instead this value (byte or int from [0, 255]) will be used for the whole
	 * layer!
	 */
	public static final String CONFIG_VALUE_LAYER2 = "jpg.value.l2";
	/**
	 * If given, the image channel associated with this layer will not be written, instead this value (byte or int from [0, 255]) will be used for the whole
	 * layer!
	 */
	public static final String CONFIG_VALUE_LAYER3 = "jpg.value.l3";
	/**
	 * If given, the image channel associated with this layer will not be written, instead this value (byte or int from [0, 255]) will be used for the whole
	 * layer!
	 */
	public static final String CONFIG_VALUE_LAYER4 = "jpg.value.l4";

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.JPEG_TYPE_1, TexType.JPEG_TYPE_2, TexType.JPEG_TYPE_3));

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return this.acceptedTypes;
	}

	@Override
	public ByteBuffer writeTextureAfterValidation(TexType target, TextureImage[] images, Map<String, Object> config) {
		assertImageFormat(images);

		final var header = new StructTextureFileHeader(true);
		header.width = images[images.length - 1].getImageWidth();
		header.height = images[images.length - 1].getImageHeight();
		// header.depth = ?; // TODO
		// header.sides = ?;
		header.mipMaps = images.length;
		header.format = target.getFormat();
		header.isCompressed = target.isCompressed();
		header.compressionFormat = target.getCompressionFormat();
		header.imageSizesCount = images.length;

		{
			final var quality = (byte) config.getOrDefault(CONFIG_QUALITY, 100);
			header.layerInfos[0].setQuality((byte) config.getOrDefault(CONFIG_QUALITY_LAYER1, quality));
			header.layerInfos[1].setQuality((byte) config.getOrDefault(CONFIG_QUALITY_LAYER2, quality));
			header.layerInfos[2].setQuality((byte) config.getOrDefault(CONFIG_QUALITY_LAYER3, quality));
			header.layerInfos[3].setQuality((byte) config.getOrDefault(CONFIG_QUALITY_LAYER4, quality));
		}

		if (config.containsKey(CONFIG_VALUE_LAYER1)) {
			header.layerInfos[0].hasReplacement(true);
			header.layerInfos[0].setReplacement((byte) config.get(CONFIG_VALUE_LAYER1));
		}

		if (config.containsKey(CONFIG_VALUE_LAYER2)) {
			header.layerInfos[1].hasReplacement(true);
			header.layerInfos[1].setReplacement((byte) config.get(CONFIG_VALUE_LAYER2));
		}

		if (config.containsKey(CONFIG_VALUE_LAYER3)) {
			header.layerInfos[2].hasReplacement(true);
			header.layerInfos[2].setReplacement((byte) config.get(CONFIG_VALUE_LAYER3));
		}

		if (config.containsKey(CONFIG_VALUE_LAYER4)) {
			header.layerInfos[3].hasReplacement(true);
			header.layerInfos[3].setReplacement((byte) config.get(CONFIG_VALUE_LAYER4));
		}

		final var encoder = new JPGEncoder(target, //
				new boolean[] { //
						header.layerInfos[0].hasReplacement(), //
						header.layerInfos[1].hasReplacement(), //
						header.layerInfos[2].hasReplacement(), //
						header.layerInfos[3].hasReplacement() //
				}, new int[] { //
						header.layerInfos[0].getQuality(), //
						header.layerInfos[1].getQuality(), //
						header.layerInfos[2].getQuality(), //
						header.layerInfos[3].getQuality() //
				});

		final var imageData = new ByteBuffer[images.length];
		var paddedImageDataSize = 0;
		for (var i = 0; i < images.length; ++i) {
			final var image = images[i];
			final var encodedImageData = encoder.encode(image.getImageData(), image.getImageWidth(), image.getImageHeight());
			final var paddedLength = ByteAlignmentUtil.alignTo16Byte(encodedImageData.remaining());
			header.imageSizes[i] = paddedLength;
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

	private void assertImageFormat(TextureImage[] images) {
		for (int i = images.length - 1; 0 <= i; --i) {
			if (images[i].getImageFormat() != TextureImageFormat.ARGB) {
				throw new TextureEncodingException(String.format("Expected image format %s. TextureImageFormat of image %d was %s. ", TextureImageFormat.ARGB,
						i, images[i].getImageFormat()));
			}
		}
	}

}
