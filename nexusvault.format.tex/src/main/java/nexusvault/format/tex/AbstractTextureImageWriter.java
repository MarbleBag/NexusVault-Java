package nexusvault.format.tex;

import nexusvault.format.tex.jpg.TextureJPGEncodingException;

public abstract class AbstractTextureImageWriter implements TextureImageWriter {

	protected void assertImageOrder(TextureImage[] images) {
		for (int i = images.length - 2; 0 <= i; --i) {
			if (images[i].getImageWidth() > images[i + 1].getImageWidth() || images[i].getImageHeight() > images[i + 1].getImageHeight()) {
				throw new TextureJPGEncodingException(
						String.format("Images need to be sorted from smallest to largest image. Error at indices %d and %d", i, i + 1));
			}
		}
	}

	protected void assertImageData(TextureImage[] images) {
		if (images.length > 13) {
			throw new IllegalArgumentException();
		}

		int expectedHeight = images[images.length - 1].getImageHeight();
		int expectedWidth = images[images.length - 1].getImageWidth();

		for (int i = images.length - 1; 0 <= i; --i) {
			final int height = images[i].getImageHeight();
			final int width = images[i].getImageWidth();
			final int expectedBytes = height * width * images[i].getImageFormat().getBytesPerPixel();

			if (images[i].getImageData().length != expectedBytes) {
				throw new TextureJPGEncodingException(String.format("ImageData of image %d is too big or small. Expected number of bytes %d, but was %d", i,
						expectedBytes, images[i].getImageData().length));
			}

			if (expectedHeight != height) {
				throw new TextureJPGEncodingException(String.format("Height of image %d is wrong. Expected %d, but was %d", i, expectedHeight, height));
			}

			if (expectedWidth != width) {
				throw new TextureJPGEncodingException(String.format("Width of image %d is wrong. Expected %d, but was %d", i, expectedWidth, width));
			}

			expectedHeight = Math.max(1, expectedHeight >> 1);
			expectedWidth = Math.max(1, expectedWidth >> 1);
		}
	}

}
