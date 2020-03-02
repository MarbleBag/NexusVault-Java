package nexusvault.format.tex;

import java.util.Set;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public interface TextureImageReader {

	TextureImage read(StructTextureFileHeader header, BinaryReader source, int imageIdx);

	byte[] getUnprocessedImageData(StructTextureFileHeader header, BinaryReader source, int imageIdx);

	Set<TexType> getAcceptedTexTypes();

	TextureImageFormat getImageFormat();

	long calculateExpectedTextureImageSize(StructTextureFileHeader header);

}
