package nexusvault.format.tex.jpg;

import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Decodes ws jpg encoded texture files of type 1
 */
public final class JPG1TextureDataDecoder extends JPGTextureDataEncoder {

	public JPG1TextureDataDecoder() {
		this(new JPGDecoderNoSampling(new Type1PixelComposition()));
	}

	public JPG1TextureDataDecoder(JPGDecoderNoSampling decoder) {
		super(decoder);
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return header.isCompressed && (header.compressionFormat == 1);
	}

}
