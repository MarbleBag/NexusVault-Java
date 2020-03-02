package nexusvault.format.tex.util;

import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;

public final class TextureImageTypeConverter {

	public static TextureImage convertToType(TextureImage source, TextureImageFormat target) {
		if (source.getImageFormat() == target) {
			return source;
		}
		final var data = convertToType2(source, target);
		return new TextureImage(source.getImageWidth(), source.getImageHeight(), target, data);
	}

	private static byte[] convertToType2(TextureImage source, TextureImageFormat target) {
		final var image = source.getImageData();
		switch (source.getImageFormat()) {
			case ARGB:
				switch (target) {
					case GRAYSCALE:
						return ImageDataConverter.convertARGBToGrayscale(image);
					case RGB:
						return ImageDataConverter.convertARGBToRGB(image);
				}
			case GRAYSCALE:
				switch (target) {
					case ARGB:
						return ImageDataConverter.convertGrayscaleToARGB(image);
					case RGB:
						return ImageDataConverter.convertGrayscaleToRGB(image);
				}
			case RGB:
				switch (target) {
					case ARGB:
						return ImageDataConverter.convertRGBToARGB(image);
					case GRAYSCALE:
						return ImageDataConverter.convertRGBToGrayscale(image);
				}
			default:
				throw new IllegalArgumentException(String.format("Unsupported source[%s] or target format[%s]", source.getImageFormat(), target));
		}
	}

}
