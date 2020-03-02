package nexusvault.format.tex.unc;

public final class RGB565TextureImageReader extends UncompressedTextureImageReader {

	public RGB565TextureImageReader() {
		super(new RGB565ImageDecoder(), new UncompressedImageMetaCalculator(2));
	}

}
