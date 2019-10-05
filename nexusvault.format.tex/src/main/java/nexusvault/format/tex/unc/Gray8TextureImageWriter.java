package nexusvault.format.tex.unc;

public final class Gray8TextureImageWriter extends UncompressedTextureImageWriter {

	public Gray8TextureImageWriter() {
		super(new Gray8Encoder());
	}

}
