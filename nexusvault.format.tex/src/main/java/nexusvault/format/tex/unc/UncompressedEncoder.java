package nexusvault.format.tex.unc;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TextureImageFormat;

interface UncompressedEncoder {
	boolean accepts(TextureImageFormat format);

	void encode(BinaryReader source, TextureImageFormat format, int width, int height, BinaryWriter destination);
}
