package nexusvault.format.tex.unc;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureRawData;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class ARGB8888TextureDataDecoder extends AbstUncompressedTextureDataDecoder {

	final private static int BYTES_PER_PIXEL = 4;

	public ARGB8888TextureDataDecoder() {
		super(BYTES_PER_PIXEL);
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return super.accepts(header) && ((header.format == 1) || (header.format == 0));
	}

	@Override
	protected TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx) {
		final byte[] imageData = new byte[meta.length];
		data.copyTo(meta.offset, imageData, 0, meta.length);
		for (int i = 0; i < imageData.length; i += 4) {
			final var B = imageData[i + 0];
			final var G = imageData[i + 1];
			final var R = imageData[i + 2];
			final var A = imageData[i + 3];
			imageData[i + 0] = A;
			imageData[i + 1] = R;
			imageData[i + 2] = G;
			imageData[i + 3] = B;
		}
		return new TextureImage(meta.width, meta.height, TextureImageFormat.ARGB, imageData);
	}

	@Override
	public TextureImageFormat getImageFormat() {
		return TextureImageFormat.ARGB;
	}

}
