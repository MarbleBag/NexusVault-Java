package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Provides the means to digest a set of <tt>.tex</tt> data
 */
public interface TextureDataDecoder {

	long calculateTotalTextureDataSize(StructTextureFileHeader header);

	boolean accepts(StructTextureFileHeader header);

	byte[] getImageData(StructTextureFileHeader header, TextureRawData data, int idx);

	TextureImage getImage(StructTextureFileHeader header, TextureRawData data, int idx);

}
