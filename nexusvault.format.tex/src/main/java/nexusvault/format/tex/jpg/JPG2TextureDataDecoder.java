package nexusvault.format.tex.jpg;

import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Decodes ws jpg encoded texture files of type 2
 */
public final class JPG2TextureDataDecoder extends JPGTextureDataEncoder {

	public JPG2TextureDataDecoder() {
		this(new JPGDecoderNoSampling(new Type0And2PixelComposition()));
	}

	public JPG2TextureDataDecoder(JPGDecoderNoSampling decoder) {
		super(decoder);
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return header.isCompressed && (header.compressionFormat == 2);
	}

}
