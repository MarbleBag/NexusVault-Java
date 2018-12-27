package nexusvault.format.tex.dxt;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import ddsutil.DDSUtil;
import gr.zdimensions.jsquish.Squish;
import nexusvault.format.tex.AbstTextureDataDecoder;
import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.StructTextureFileHeader;
import nexusvault.format.tex.TextureChannel;
import nexusvault.format.tex.TextureChannelType;
import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureDataDecoder;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureRawData;

public final class DXTTextureDataEncoder extends AbstTextureDataDecoder implements TextureDataDecoder {

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return !header.isCompressed && ((header.format == 13) || (header.format == 14) || (header.format == 15));
	}

	@Override
	protected ImageMetaInformation getImageInformation(StructTextureFileHeader header, int idx) {
		if (header == null) {
			throw new IllegalArgumentException("'header' must not be null");
		}
		if ((idx < 0) || (header.mipMaps <= idx)) {
			throw new IndexOutOfBoundsException(String.format("'idx' out of bounds. Range [0;%d). Got %d", header.mipMaps, idx));
		}

		final boolean isCompressed = header.format == 13;
		final int blockSize = isCompressed ? 8 : 16;

		int width = 0, height = 0, length = 0, offset = 0;

		for (int i = 0; i <= idx; ++i) {
			width = (int) (header.width / Math.pow(2, header.mipMaps - 1 - i));
			height = (int) (header.height / Math.pow(2, header.mipMaps - 1 - i));
			width = width <= 0 ? 1 : width;
			height = height <= 0 ? 1 : height;

			offset += length;
			length = ((width + 3) / 4) * ((height + 3) / 4) * blockSize;
		}

		return new ImageMetaInformation(offset, length, width, height);
	}

	@Override
	protected TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx) {
		Squish.CompressionType compressionType;
		switch (header.format) {
			case 13:
				compressionType = Squish.CompressionType.DXT1;
				break;
			case 14:
				compressionType = Squish.CompressionType.DXT3;
				break;
			case 15:
				compressionType = Squish.CompressionType.DXT5;
				break;
			default:
				throw new IllegalArgumentException();
		}

		final ByteBuffer imgData = data.createView(meta.offset, meta.length);
		final BufferedImage image = DDSUtil.decompressTexture(imgData, meta.width, meta.height, compressionType);
		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
			throw new TextureConversionException();
		}

		final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] rawData = buffer.getData();
		for (int i = 0; i < rawData.length; i += 4) {
			final byte tmp = rawData[i + 1];
			rawData[i + 1] = rawData[i + 3];
			rawData[i + 3] = tmp;
		}

		final TextureChannel channel = new TextureChannel(TextureChannelType.ARGB, rawData);
		return new TextureImage(meta.width, meta.height, channel);
	}

}
