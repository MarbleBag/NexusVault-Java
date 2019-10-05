package nexusvault.format.tex;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class TextureImage {

	private final TextureImageFormat format;
	private final int width;
	private final int height;
	private final byte[] data;

	public TextureImage(int width, int height, TextureImageFormat format, byte[] data) {
		if (width <= 0) {
			throw new IllegalArgumentException();
		}
		if (height <= 0) {
			throw new IllegalArgumentException();
		}
		if (format == null) {
			throw new IllegalArgumentException();
		}
		if (data == null) {
			throw new IllegalArgumentException();
		}

		this.width = width;
		this.height = height;
		this.format = format;
		this.data = data;

		final int bytesPerPixel = format.getBytesPerPixel();
		final int expectedBytes = width * height * bytesPerPixel;
		if (data.length != expectedBytes) {
			throw new IllegalArgumentException(
					String.format("Image data does not fit an image of %dx%d of type %s. Expected number of bytes %d, actual number of bytes %d", width, height,
							format.name(), expectedBytes, data.length));
		}
	}

	public int getImageHeight() {
		return height;
	}

	public int getImageWidth() {
		return width;
	}

	public byte[] getImageData() {
		return data;
	}

	public TextureImageFormat getImageFormat() {
		return format;
	}

	public BufferedImage convertToBufferedImage() {
		BufferedImage image = null;
		switch (format) {
			case GRAYSCALE: {// grayscale
				image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
				image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(data, data.length), null));
				break;
			}

			case RGB: {// RGB
				final int[] tmp = new int[data.length / 3];
				for (int i = 0; i < tmp.length; ++i) {
					tmp[i] = 0xFF000000 | (data[(i * 3) + 0] << 16) | (data[(i * 3) + 1] << 8) | data[(i * 3) + 2];
				}
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(tmp, tmp.length), null));
				break;
			}

			case ARGB: {// ARGB
				final int[] tmp = new int[data.length / Integer.BYTES];
				ByteBuffer.wrap(data).asIntBuffer().get(tmp);
				image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(tmp, tmp.length), null));
				break;
			}

			default:
				throw new IllegalArgumentException();
		}
		return image;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TextureImage [format=");
		builder.append(format);
		builder.append(", width=");
		builder.append(width);
		builder.append(", height=");
		builder.append(height);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(data);
		result = (prime * result) + ((format == null) ? 0 : format.hashCode());
		result = (prime * result) + height;
		result = (prime * result) + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TextureImage other = (TextureImage) obj;
		if (!Arrays.equals(data, other.data)) {
			return false;
		}
		if (format != other.format) {
			return false;
		}
		if (height != other.height) {
			return false;
		}
		if (width != other.width) {
			return false;
		}
		return true;
	}

}
