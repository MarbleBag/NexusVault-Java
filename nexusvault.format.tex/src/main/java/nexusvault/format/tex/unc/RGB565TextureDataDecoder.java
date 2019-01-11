package nexusvault.format.tex.unc;

import java.nio.ShortBuffer;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureRawData;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class RGB565TextureDataDecoder extends AbstUncompressedTextureDataDecoder {

	final private static int BYTES_PER_PIXEL = 2;

	public RGB565TextureDataDecoder() {
		super(BYTES_PER_PIXEL);
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return super.accepts(header) && (header.format == 5);
	}

	@Override
	protected TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx) {
		final int numberOfPixels = meta.length / BYTES_PER_PIXEL;
		final byte[] imageData = new byte[numberOfPixels * 3];
		final ShortBuffer tmp = data.createView(meta.offset, meta.length).asShortBuffer();

		int pixelIndex = 0;
		while (tmp.hasRemaining()) {
			final short n = tmp.get();
			imageData[pixelIndex + 0] = (byte) ((n >> 11) & 0xFF);
			imageData[pixelIndex + 1] = (byte) ((n >> 5) & 0xFF);
			imageData[pixelIndex + 2] = (byte) ((n >> 0) & 0xFF);
			pixelIndex += 1;
		}

		if (numberOfPixels != pixelIndex) {
			throw new TextureConversionException("Unable to convert R5G6B5 to R8G8B8");
		}

		return new TextureImage(meta.width, meta.height, TextureImageFormat.RGB, imageData);
	}

}
