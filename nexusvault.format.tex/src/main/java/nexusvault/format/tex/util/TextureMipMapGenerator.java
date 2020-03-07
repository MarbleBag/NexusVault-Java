package nexusvault.format.tex.util;

import java.awt.image.BufferedImage;

import nexusvault.format.tex.TextureImage;

public final class TextureMipMapGenerator {

	public static TextureImage[] buildMipMaps(TextureImage src, int numberOfMipMaps) {
		final var tmp = new BufferedImage[numberOfMipMaps];
		tmp[0] = TextureImageAwtConverter.convertToBufferedImage(src);
		for (int i = 1; i < numberOfMipMaps; ++i) {
			tmp[i] = scaleDown(tmp[i - 1]);
		}

		final var images = new TextureImage[numberOfMipMaps];
		for (int i = 0; i < images.length; ++i) {
			images[i] = TextureImageAwtConverter.convertToTextureImage(src.getImageFormat(), tmp[i]);
		}

		return images;
	}

	private static BufferedImage scaleDown(BufferedImage input) {
		final var newWidth = Math.max(1, input.getWidth() >> 1);
		final var newHeight = Math.max(1, input.getHeight() >> 1);

		final var output = new BufferedImage(newWidth, newHeight, input.getType());
		final var g2d = output.createGraphics();
		g2d.drawImage(input, 0, 0, newWidth, newHeight, null);
		g2d.dispose();

		return output;
	}

}
