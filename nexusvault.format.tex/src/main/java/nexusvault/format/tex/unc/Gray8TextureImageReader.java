package nexusvault.format.tex.unc;

public final class Gray8TextureImageReader extends UncompressedTextureImageReader {

	public Gray8TextureImageReader() {
		super(new Gray8Decoder(), new UncompressedImageMetaCalculator(1));
	}

}
