package nexusvault.format.tex;

public interface TextureDataDecoder {

	long calculateTotalTextureDataSize(StructTextureFileHeader header);

	boolean accepts(StructTextureFileHeader header);

	byte[] getImageData(StructTextureFileHeader header, TextureRawData data, int idx);

	TextureImage getImage(StructTextureFileHeader header, TextureRawData data, int idx);

}
