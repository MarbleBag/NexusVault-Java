package nexusvault.format.tex.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

import nexusvault.format.tex.TextureImage;

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
	
}