package nexusvault.format.tex.dxt;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import ddsutil.DDSUtil;
import gr.zdimensions.jsquish.Squish;
import gr.zdimensions.jsquish.Squish.CompressionType;
import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;

public class DXTEncoder {

	private final CompressionType compressionType;

	public DXTEncoder(TexType target) {
		compressionType = getCompressionType(target);
	}

	private Squish.CompressionType getCompressionType(TexType target) {
		switch (target) {
			case DXT1:
				return Squish.CompressionType.DXT1;
			case DXT3:
				return Squish.CompressionType.DXT3;
			case DXT5:
				return Squish.CompressionType.DXT5;
			default:
				throw new IllegalArgumentException(/* TODO */);
		}
	}

	public void encode(BinaryReader source, TextureImageFormat format, int width, int height, BinaryWriter destination) {
		final byte[] data = new byte[width * height * format.getBytesPerPixel()];
		source.readInt8(data, 0, data.length);

		final TextureImage image = new TextureImage(width, height, format, data);
		final BufferedImage bufferedImage = image.convertToBufferedImage();

		final ByteBuffer compressed = DDSUtil.compressTexture(bufferedImage, compressionType);

		final int transfered = destination.write(compressed);
		if ((transfered == 0) || compressed.hasRemaining()) {
			throw new TextureConversionException(/* TODO */);
		}
	}

	public boolean accepts(TextureImageFormat format) {
		return true;
	}

}
