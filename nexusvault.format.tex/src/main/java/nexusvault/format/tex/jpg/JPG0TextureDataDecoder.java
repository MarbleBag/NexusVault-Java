package nexusvault.format.tex.jpg;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureRawData;
import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Decodes ws jpg encoded texture files of type 0, which use chroma sub sampling
 */
public final class JPG0TextureDataDecoder extends JPGTextureDataEncoder {

	private final JPGDecoderChromaSubsampling decoder;

	public JPG0TextureDataDecoder() {
		this(new JPGDecoderChromaSubsampling(new DefaultPixelCompositionProvider()));
	}

	public JPG0TextureDataDecoder(JPGDecoderChromaSubsampling decoder) {
		this.decoder = decoder;
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return header.isCompressed && (header.compressionFormat == 0);
	}

	@Override
	protected TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx) {
		return decoder.decodeImage(header, data, meta);
	}

}
