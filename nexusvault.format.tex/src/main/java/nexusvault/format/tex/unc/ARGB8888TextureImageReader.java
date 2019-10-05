package nexusvault.format.tex.unc;

public final class ARGB8888TextureImageReader extends UncompressedTextureImageReader {

	public ARGB8888TextureImageReader() {
		super(new ARGB8888Decoder(), new UncompressedImageMetaCalculator(4));
	}

}
