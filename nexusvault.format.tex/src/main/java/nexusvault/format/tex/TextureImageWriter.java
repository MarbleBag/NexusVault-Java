package nexusvault.format.tex;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;

public interface TextureImageWriter {
	void write(BinaryReader source, TextureImageFormat format, int width, int height, BinaryWriter destination);
}
