package nexusvault.format.tex.unc;

public final class ARGB8888TextureImageWriter extends UncompressedTextureImageWriter {

	public ARGB8888TextureImageWriter() {
		super(new ARGB8888Encoder());
	}

}
