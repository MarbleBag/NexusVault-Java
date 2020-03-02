package nexusvault.format.tex.unc;

public final class ARGB8888TextureImageReader extends UncompressedTextureImageReader {

	public ARGB8888TextureImageReader() {
		super(new ARGB8888ImageDecoder(), new UncompressedImageMetaCalculator(4));
	}

}
