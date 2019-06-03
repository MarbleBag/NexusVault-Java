package nexusvault.format.tex.jpg;

import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Decodes ws jpg encoded texture files of type 0, which use chroma sub sampling
 */
public final class JPG0TextureDataDecoder extends JPGTextureDataEncoder {

	public JPG0TextureDataDecoder() {
		this(new JPGDecoderChromaSubsampling(new Type0And2PixelComposition()));
	}

	public JPG0TextureDataDecoder(JPGDecoderChromaSubsampling decoder) {
		super(decoder);
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return header.isCompressed && (header.compressionFormat == 0);
	}

}
