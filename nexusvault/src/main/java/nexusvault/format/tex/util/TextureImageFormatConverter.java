package nexusvault.format.tex.util;

import nexusvault.format.tex.Image;
import nexusvault.format.tex.Image.ImageFormat;

public final class TextureImageFormatConverter {
	private TextureImageFormatConverter() {
	}

	public static Image convertToType(Image source, ImageFormat target) {
		if (source.getFormat() == target) {
			return source;
		}
		final var data = convertColorModel(source, target);
		return new Image(source.getWidth(), source.getHeight(), target, data);
	}

	@SuppressWarnings("incomplete-switch")
	private static byte[] convertColorModel(Image source, ImageFormat target) {
		final var image = source.getData();
		switch (source.getFormat()) {
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
		throw new IllegalArgumentException(String.format("Unsupported source[%s] or target format[%s]", source.getFormat(), target));
	}

}
