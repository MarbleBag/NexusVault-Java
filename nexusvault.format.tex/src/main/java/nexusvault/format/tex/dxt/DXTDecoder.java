package nexusvault.format.tex.dxt;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import ddsutil.DDSUtil;
import gr.zdimensions.jsquish.Squish;
import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureConversionException;

public final class DXTDecoder {

	private final Squish.CompressionType compressionType;

	public DXTDecoder(TexType target) {
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

	public byte[] decode(BinaryReader source, int byteLength, int width, int height) {
		final byte[] data = new byte[byteLength];
		source.readInt8(data, 0, data.length);
		final BufferedImage image = DDSUtil.decompressTexture(data, width, height, compressionType);

		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
			throw new TextureConversionException();
		}

		final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] imageData = buffer.getData();
		for (int i = 0; i < imageData.length; i += 4) {
			final byte tmp = imageData[i + 1];
			imageData[i + 1] = imageData[i + 3];
			imageData[i + 3] = tmp;
		}
		return imageData;
	}

}
