package nexusvault.format.tex.jpg;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.StructTextureFileHeader;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureRawData;

/**
 * Decodes ws jpg encoded texture files of type 1
 */
public class JPG1TextureDataDecoder extends JPGTextureDataEncoder {

	private final JPGDecoderNoSampling decoder = new JPGDecoderNoSampling();

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return header.isCompressed && (header.compressionFormat == 1);
	}

	@Override
	protected TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx) {
		return decoder.decodeImage(header, data, meta);
	}

}
