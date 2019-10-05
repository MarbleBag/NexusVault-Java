package nexusvault.format.tex.unc;

public final class RGB565TextureImageWriter extends UncompressedTextureImageWriter {

	public RGB565TextureImageWriter() {
		super(new RGB565Encoder());
	}

}
