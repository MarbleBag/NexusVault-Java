package nexusvault.format.tex.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;

public final class TextureImageAwtConverter {

	public static BufferedImage convertToBufferedImage(TextureImage image) {
		BufferedImage result = null;
		byte[] resultData = null;

		switch (image.getImageFormat()) {
			case GRAYSCALE: {
				result = new BufferedImage(image.getImageWidth(), image.getImageHeight(), BufferedImage.TYPE_BYTE_GRAY);
				resultData = new byte[image.getImageData().length];
				System.arraycopy(image.getImageData(), 0, resultData, 0, resultData.length);
				break;
			}
			case RGB: {
				result = new BufferedImage(image.getImageWidth(), image.getImageHeight(), BufferedImage.TYPE_3BYTE_BGR);
				resultData = ImageDataConverter.convertRGBToBGR(image.getImageData());
				break;
			}
			case ARGB: {
				result = new BufferedImage(image.getImageWidth(), image.getImageHeight(), BufferedImage.TYPE_4BYTE_ABGR);
				resultData = ImageDataConverter.convertARGBToABGR(image.getImageData());
				break;
			}
		}

		result.setData(Raster.createRaster(result.getSampleModel(), new DataBufferByte(resultData, resultData.length), null));
		return result;
	}

	/**
	 * Converts a {@link BufferedImage} to {@link TextureImage} with the given {@link TextureImageFormat}.
	 * <p>
	 * <b>Does not support all types of {@link BufferedImage}</b>
	 * <p>
	 *
	 * The {@link BufferedImage#getType() type} needs to be one of
	 * <ul>
	 * <li>{@link BufferedImage#TYPE_BYTE_GRAY}
	 * <li>{@link BufferedImage#TYPE_3BYTE_BGR}
	 * <li>{@link BufferedImage#TYPE_4BYTE_ABGR}
	 * </ul>
	 *
	 * And the used {@link DataBuffer} needs to be an instance of {@link DataBufferByte}
	 *
	 * @param format
	 *            the {@link TextureImageFormat}
	 * @param bufferedImage
	 *            Image which can be converted
	 * @return a {@link TextureImage} created from the given image
	 * @throws TextureConversionException
	 *             if the image is not convertible
	 */
	public static TextureImage convertToTextureImage(TextureImageFormat format, BufferedImage bufferedImage) {
		final var bufferedImageType = bufferedImage.getType();
		switch (bufferedImageType) {
			case BufferedImage.TYPE_3BYTE_BGR:
			case BufferedImage.TYPE_4BYTE_ABGR:
			case BufferedImage.TYPE_BYTE_GRAY:
				break;
			default:
				throw new TextureConversionException(String.format("BufferedImage type %d not supported", bufferedImageType));
		}

		final var dataBuffer = bufferedImage.getData().getDataBuffer();
		if (!(dataBuffer instanceof DataBufferByte)) {
			throw new TextureConversionException(String.format("BufferedImage DataBuffer type %s not supported", dataBuffer.getClass()));
		}

		final var srcData = ((DataBufferByte) dataBuffer).getData();
		byte[] dstData = null;

		switch (format) {
			case GRAYSCALE:
				dstData = srcData;
				break;
			case RGB:
				dstData = ImageDataConverter.convertBGRToRGB(srcData);
				break;
			case ARGB:
				dstData = ImageDataConverter.convertABGRToARGB(srcData);
				break;
		}

		return new TextureImage(bufferedImage.getWidth(), bufferedImage.getHeight(), format, dstData);
	}

}