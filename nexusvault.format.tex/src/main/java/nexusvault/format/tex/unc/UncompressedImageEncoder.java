package nexusvault.format.tex.unc;

import nexusvault.format.tex.TextureImageFormat;

interface UncompressedImageEncoder {

	byte[] encode(byte[] source, int width, int height, TextureImageFormat format);

}
