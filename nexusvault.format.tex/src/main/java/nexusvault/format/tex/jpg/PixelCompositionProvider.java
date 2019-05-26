package nexusvault.format.tex.jpg;

import nexusvault.format.tex.struct.StructTextureFileHeader;

public interface PixelCompositionProvider {
	PixelCompositionStrategy getPixelCalculator(StructTextureFileHeader header);
}