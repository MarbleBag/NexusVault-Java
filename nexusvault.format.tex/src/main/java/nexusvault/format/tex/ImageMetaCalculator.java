package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructTextureFileHeader;

public interface ImageMetaCalculator {
	ImageMetaInformation getImageInformation(StructTextureFileHeader header, int mipmapIndex);
}
