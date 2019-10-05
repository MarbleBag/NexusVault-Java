package nexusvault.format.tex.unc;

import java.util.Set;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;

interface UncompressedDecoder {
	byte[] decode(BinaryReader source, int byteLength);

	TextureImageFormat getReturnedImageFormat();

	Set<TexType> getAcceptedTexTypes();
}
