package nexusvault.format.tex.dxt;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureImageWriter;

public final class DXT1TextureImageWriter implements TextureImageWriter {

	private final DXTEncoder encoder = new DXTEncoder(TexType.DXT1);

	@Override
	public void write(BinaryReader source, TextureImageFormat format, int width, int height, BinaryWriter destination) {
		encoder.encode(source, format, width, height, destination);
	}

}
