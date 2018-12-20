package nexusvault.format.tex;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class TextureImage {

	private final int width;
	private final int height;
	private final TextureChannel[] channels;

	public TextureImage(int width, int height, TextureChannel... channels) {
		if (width <= 0) {
			throw new IllegalArgumentException();
		}
		if (height <= 0) {
			throw new IllegalArgumentException();
		}
		if (channels == null) {
			throw new IllegalArgumentException();
		}

		this.width = width;
		this.height = height;
		this.channels = channels;

		for (int i = 0; i < channels.length; ++i) {
			final TextureChannel channel = channels[i];
			final int bytesPerPixel = channel.type.getBytesPerPixel();
			final int expectedBytes = width * height * bytesPerPixel;
			if (channel.data.length != expectedBytes) {
				throw new IllegalArgumentException(
						String.format("Channel [%d] %s does not fit an image of %dx%d of type %s. Expected number of bytes %d, actual number of bytes %d", i,
								channel, width, height, channel.type.name(), expectedBytes, channel.data.length));
			}
		}
	}

	public int getImageHeight() {
		return height;
	}

	public int getImageWidth() {
		return width;
	}

	public TextureChannel getChannel(int channelIdx) {
		return channels[channelIdx];
	}

	public int getChannelCount() {
		return channels.length;
	}

	public final List<BufferedImage> convertToBufferedImage() {
		final List<BufferedImage> result = new LinkedList<>();
		for (final TextureChannel channel : channels) {
			BufferedImage image = null;
			switch (channel.type) {
				case GRAYSCALE: {// grayscale
					image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
					image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(channel.data, channel.data.length), null));
					break;
				}

				case RGB: {// RGB
					final int[] tmp = new int[channel.data.length / 3];
					for (int i = 0; i < tmp.length; ++i) {
						tmp[i] = 0xFF000000 | (channel.data[(i * 3) + 0] << 16) | (channel.data[(i * 3) + 1] << 8) | channel.data[(i * 3) + 2];
					}
					image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(tmp, tmp.length), null));
					break;
				}

				case ARGB: {// ARGB
					final int[] tmp = new int[channel.data.length / Integer.BYTES];
					ByteBuffer.wrap(channel.data).asIntBuffer().get(tmp);
					image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(tmp, tmp.length), null));
					break;
				}

				default:
					throw new IllegalArgumentException();
			}
			result.add(image);
		}
		return result;
	}
}
