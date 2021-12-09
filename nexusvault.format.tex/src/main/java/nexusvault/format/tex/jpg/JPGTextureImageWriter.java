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

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.JPG1, TexType.JPG2, TexType.JPG3));

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
		header.depth = (int) config.getOrDefault(CONFIG_DEPTH, 1);
		header.sides = (int) config.getOrDefault(CONFIG_SIDES, 1);
		header.mipMaps = images.length;
		header.format = target.getFormat();
		header.isJpg = target.isJpg();
		header.jpgFormat = target.getJpgFormat();
		header.mipmapSizesCount = images.length;

		{
			final var quality = ((Number) config.getOrDefault(CONFIG_QUALITY, 100)).byteValue();
			header.jpgChannelInfos[0].setQuality(((Number) config.getOrDefault(CONFIG_QUALITY_LAYER1, quality)).byteValue());
			header.jpgChannelInfos[1].setQuality(((Number) config.getOrDefault(CONFIG_QUALITY_LAYER2, quality)).byteValue());
			header.jpgChannelInfos[2].setQuality(((Number) config.getOrDefault(CONFIG_QUALITY_LAYER3, quality)).byteValue());
			header.jpgChannelInfos[3].setQuality(((Number) config.getOrDefault(CONFIG_QUALITY_LAYER4, quality)).byteValue());
		}

		if (config.containsKey(CONFIG_VALUE_LAYER1)) {
			header.jpgChannelInfos[0].hasDefaultColor(true);
			header.jpgChannelInfos[0].setDefaultColor(((Number) config.get(CONFIG_VALUE_LAYER1)).byteValue());
		}

		if (config.containsKey(CONFIG_VALUE_LAYER2)) {
			header.jpgChannelInfos[1].hasDefaultColor(true);
			header.jpgChannelInfos[1].setDefaultColor(((Number) config.get(CONFIG_VALUE_LAYER2)).byteValue());
		}

		if (config.containsKey(CONFIG_VALUE_LAYER3)) {
			header.jpgChannelInfos[2].hasDefaultColor(true);
			header.jpgChannelInfos[2].setDefaultColor(((Number) config.get(CONFIG_VALUE_LAYER3)).byteValue());
		}

		if (config.containsKey(CONFIG_VALUE_LAYER4)) {
			header.jpgChannelInfos[3].hasDefaultColor(true);
			header.jpgChannelInfos[3].setDefaultColor(((Number) config.get(CONFIG_VALUE_LAYER4)).byteValue());
		}

		final var encoder = new JPGEncoder(target, //
				new boolean[] { //
						header.jpgChannelInfos[0].hasDefaultColor(), //
						header.jpgChannelInfos[1].hasDefaultColor(), //
						header.jpgChannelInfos[2].hasDefaultColor(), //
						header.jpgChannelInfos[3].hasDefaultColor() //
				}, new int[] { //
						header.jpgChannelInfos[0].getQuality(), //
						header.jpgChannelInfos[1].getQuality(), //
						header.jpgChannelInfos[2].getQuality(), //
						header.jpgChannelInfos[3].getQuality() //
				});

		final var imageData = new ByteBuffer[images.length];
		var paddedImageDataSize = 0;
		for (var i = 0; i < images.length; ++i) {
			try {
				final var image = images[i];
				final var encodedImageData = encoder.encode(image.getImageData(), image.getImageWidth(), image.getImageHeight());
				final var paddedLength = ByteAlignmentUtil.alignTo16Byte(encodedImageData.remaining());
				header.mipmapSizes[i] = paddedLength;
				paddedImageDataSize += header.mipmapSizes[i];
				imageData[i] = encodedImageData;
			} catch (final Exception e) {
				throw new TextureEncodingException(String.format("Unable to encode mipmap #%d", i + 1), e);
			}
		}

		final var output = ByteBuffer.allocate(StructTextureFileHeader.SIZE_IN_BYTES + paddedImageDataSize).order(ByteOrder.LITTLE_ENDIAN);
		kreed.reflection.struct.StructUtil.writeStruct(header, output, true);

		for (var i = 0; i < images.length; ++i) {
			final var remainder = header.mipmapSizes[i] - imageData[i].remaining();
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
