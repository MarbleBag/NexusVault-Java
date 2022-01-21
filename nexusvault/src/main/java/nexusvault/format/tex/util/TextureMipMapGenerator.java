package nexusvault.format.tex.util;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import nexusvault.format.tex.Image;

public final class TextureMipMapGenerator {
	private TextureMipMapGenerator() {
	}

	public static Image[] buildMipMaps(Image src, int numberOfMipMaps) {
		if (numberOfMipMaps == 0) {
			throw new IllegalArgumentException("Argument: 'numberofMipMaps' must not be 0");
		}

		if (numberOfMipMaps == 1) {
			return new Image[] { src };
		}

		if (numberOfMipMaps < 0) {
			final var value = Math.min(src.getWidth(), src.getHeight());
			numberOfMipMaps = (int) Math.ceil(Math.log(value) / Math.log(2));
		}

		final var tmp = new BufferedImage[numberOfMipMaps];
		tmp[0] = AwtImageConverter.convertToBufferedImage(src);
		for (int i = 1; i < numberOfMipMaps; ++i) {
			tmp[i] = scaleDown(tmp[i - 1]);
		}

		final var images = new Image[numberOfMipMaps];
		for (int i = 0; i < images.length; ++i) {
			images[i] = AwtImageConverter.convertToTextureImage(src.getFormat(), tmp[i]);
		}

		return images;
	}

	private static BufferedImage scaleDown(BufferedImage input) {
		final var newWidth = Math.max(1, input.getWidth() >> 1);
		final var newHeight = Math.max(1, input.getHeight() >> 1);

		final var output = new BufferedImage(newWidth, newHeight, input.getType());
		final var g2d = output.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.drawImage(input, 0, 0, newWidth, newHeight, null);
		g2d.dispose();

		return output;
	}

}
