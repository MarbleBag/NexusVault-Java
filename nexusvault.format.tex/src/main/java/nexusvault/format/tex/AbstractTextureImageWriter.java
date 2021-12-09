package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractTextureImageWriter implements TextureImageWriter {

	protected abstract ByteBuffer writeTextureAfterValidation(TexType target, TextureImage[] images, Map<String, Object> config);

	@Override
	public final ByteBuffer writeTexture(TexType target, TextureImage[] images, Map<String, Object> config) {
		Objects.requireNonNull(images, "images must not be null");
		Objects.requireNonNull(config, "config must not be null");

		assertTexType(target);
		assertImageOrder(images);
		assertImageSizes(images);

		return writeTextureAfterValidation(target, images, config);
	}

	private void assertTexType(TexType target) {
		if (!getAcceptedTexTypes().contains(target)) {
			throw new TextureEncodingException(String.format("TexType %s is not supported by this writer", target));
		}
	}

	private void assertImageOrder(TextureImage[] images) {
		for (int i = images.length - 2; 0 <= i; --i) {
			if (images[i].getImageWidth() > images[i + 1].getImageWidth() || images[i].getImageHeight() > images[i + 1].getImageHeight()) {
				throw new TextureEncodingException(
						String.format("Images need to be sorted from smallest to largest image. Error at indices %d and %d", i, i + 1));
			}
		}
	}

	private void assertImageSizes(TextureImage[] images) {
		if (images.length > 13) {
			throw new TextureEncodingException(String.format("Mip mapping only supports up to 13 images, was %d.", images.length));
		}

		int expectedHeight = images[images.length - 1].getImageHeight();
		int expectedWidth = images[images.length - 1].getImageWidth();

		for (int i = images.length - 1; 0 <= i; --i) {
			final int height = images[i].getImageHeight();
			final int width = images[i].getImageWidth();
			final int expectedBytes = height * width * images[i].getImageFormat().getBytesPerPixel();

			if (images[i].getImageData().length != expectedBytes) {
				throw new TextureEncodingException(String.format("ImageData of image %d is too big or small. Expected number of bytes %d, but was %d", i,
						expectedBytes, images[i].getImageData().length));
			}

			if (expectedHeight != height) {
				throw new TextureEncodingException(String.format("Height of image %d is wrong. Expected %d, but was %d", i, expectedHeight, height));
			}

			if (expectedWidth != width) {
				throw new TextureEncodingException(String.format("Width of image %d is wrong. Expected %d, but was %d", i, expectedWidth, width));
			}

			expectedHeight = Math.max(1, expectedHeight >> 1);
			expectedWidth = Math.max(1, expectedWidth >> 1);
		}
	}

}
