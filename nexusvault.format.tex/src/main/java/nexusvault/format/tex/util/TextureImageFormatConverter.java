package nexusvault.format.tex.util;

import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;

public final class TextureImageFormatConverter {

	public static TextureImage convertToType(TextureImage source, TextureImageFormat target) {
		if (source.getImageFormat() == target) {
			return source;
		}
		final var data = convertColorModel(source, target);
		return new TextureImage(source.getImageWidth(), source.getImageHeight(), target, data);
	}

	@SuppressWarnings("incomplete-switch")
	private static byte[] convertColorModel(TextureImage source, TextureImageFormat target) {
		final var image = source.getImageData();
		switch (source.getImageFormat()) {
			case ARGB:
				switch (target) {
					case GRAYSCALE:
						return ColorModelConverter.convertARGBToGrayscale(image);
					case RGB:
						return ColorModelConverter.convertARGBToRGB(image);
				}
				break;
			case GRAYSCALE:
				switch (target) {
					case ARGB:
						return ColorModelConverter.convertGrayscaleToARGB(image);
					case RGB:
						return ColorModelConverter.convertGrayscaleToRGB(image);
				}
				break;
			case RGB:
				switch (target) {
					case ARGB:
						return ColorModelConverter.convertRGBToARGB(image);
					case GRAYSCALE:
						return ColorModelConverter.convertRGBToGrayscale(image);
				}
				break;
		}
		throw new IllegalArgumentException(String.format("Unsupported source[%s] or target format[%s]", source.getImageFormat(), target));
	}

}
