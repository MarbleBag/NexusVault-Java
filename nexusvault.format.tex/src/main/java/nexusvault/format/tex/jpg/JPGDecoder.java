package nexusvault.format.tex.jpg;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.struct.StructTextureFileHeader;

interface JPGDecoder {

	byte[] decode(StructTextureFileHeader header, BinaryReader source, int byteLength, int width, int height);

}
