package nexusvault.format.tex.uncompressed;

import nexusvault.format.tex.Image;
import nexusvault.format.tex.TextureType;
import nexusvault.format.tex.util.ColorModelConverter;

public final class PlainImageWriter {
	private PlainImageWriter() {
	}

	public static byte[] getBinary(TextureType target, Image image) {
		switch (target) {
			case ARGB1:
			case ARGB2: {
				switch (image.getFormat()) {
					case ARGB:
						return ColorModelConverter.convertARGBToBGRA(image.getData());
					case RGB:
						return ColorModelConverter.convertRGBToBGRA(image.getData());
					case GRAYSCALE:
						return ColorModelConverter.convertGrayscaleToARGB(image.getData());
				}
				break;
			}
			case GRAYSCALE: {
				switch (image.getFormat()) {
					case ARGB:
						return ColorModelConverter.convertARGBToGrayscale(image.getData());
					case RGB:
						return ColorModelConverter.convertRGBToGrayscale(image.getData());
					case GRAYSCALE:
						return image.getData();
				}
				break;
			}
			case RGB: {
				switch (image.getFormat()) {
					case ARGB:
						return ColorModelConverter.packARGBToB5G6B5(image.getData());
					case RGB:
						return ColorModelConverter.packRGBToB5G6B5(image.getData());
					case GRAYSCALE:
						return ColorModelConverter.packGrayscaleToB5G6B5(image.getData());
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Invalid texture type: " + target);
		}

		throw new IllegalArgumentException(String.format("Unable to write image with format %s as %s", image.getFormat(), target));
	}
}
