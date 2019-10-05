package nexusvault.format.tex.unc;

import java.util.Objects;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureImageWriter;

abstract class UncompressedTextureImageWriter implements TextureImageWriter {

	private final UncompressedEncoder encoder;

	public UncompressedTextureImageWriter(UncompressedEncoder encoder) {
		this.encoder = Objects.requireNonNull(encoder);
	}

	@Override
	public void write(BinaryReader source, TextureImageFormat format, int width, int height, BinaryWriter destination) {
		if (!encoder.accepts(format)) {
			throw new TextureConversionException(/* TODO */);
		}
		encoder.encode(source, format, width, height, destination);
	}

}
