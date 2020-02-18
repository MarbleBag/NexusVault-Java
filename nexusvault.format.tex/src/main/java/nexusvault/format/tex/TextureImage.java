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
		return this.height;
	}

	public int getImageWidth() {
		return this.width;
	}

	public byte[] getImageData() {
		return this.data;
	}

	public TextureImageFormat getImageFormat() {
		return this.format;
	}

	public BufferedImage convertToBufferedImage() {
		BufferedImage image = null;
		switch (this.format) {
			case GRAYSCALE: {// grayscale
				image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_GRAY);
				image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(this.data, this.data.length), null));
				break;
			}

			case RGB: {// RGB
				final int[] tmp = new int[this.data.length / 3];
				for (int i = 0; i < tmp.length; ++i) {
					tmp[i] = 0xFF000000 | this.data[i * 3 + 0] << 16 | this.data[i * 3 + 1] << 8 | this.data[i * 3 + 2];
				}
				image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
				image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(tmp, tmp.length), null));
				break;
			}

			case ARGB: {// ARGB
				final int[] tmp = new int[this.data.length / Integer.BYTES];
				ByteBuffer.wrap(this.data).asIntBuffer().get(tmp);
				image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
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
		builder.append(this.format);
		builder.append(", width=");
		builder.append(this.width);
		builder.append(", height=");
		builder.append(this.height);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.data);
		result = prime * result + (this.format == null ? 0 : this.format.hashCode());
		result = prime * result + this.height;
		result = prime * result + this.width;
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
		if (!Arrays.equals(this.data, other.data)) {
			return false;
		}
		if (this.format != other.format) {
			return false;
		}
		if (this.height != other.height) {
			return false;
		}
		if (this.width != other.width) {
			return false;
		}
		return true;
	}

}
