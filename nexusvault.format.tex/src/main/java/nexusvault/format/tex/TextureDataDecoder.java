package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Provides the means to digest a set of <code>.tex</code> data
 */
public interface TextureDataDecoder {

	TextureImageFormat getImageFormat();

	boolean accepts(StructTextureFileHeader header);

	long calculateTotalTextureDataSize(StructTextureFileHeader header);

	byte[] getImageData(StructTextureFileHeader header, TextureRawData data, int idx);

	TextureImage getImage(StructTextureFileHeader header, TextureRawData data, int idx);

}
