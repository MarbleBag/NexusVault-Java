package nexusvault.format.tex.unc;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureRawData;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class Gray8TextureDataDecoder extends AbstUncompressedTextureDataDecoder {

	final private static int BYTES_PER_PIXEL = 1;

	public Gray8TextureDataDecoder() {
		super(BYTES_PER_PIXEL);
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return super.accepts(header) && (header.format == 6);
	}

	@Override
	protected TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx) {
		final byte[] imageData = new byte[meta.length];
		data.copyTo(meta.offset, imageData, 0, meta.length);
		return new TextureImage(meta.width, meta.height, TextureImageFormat.GRAYSCALE, imageData);
	}

}
