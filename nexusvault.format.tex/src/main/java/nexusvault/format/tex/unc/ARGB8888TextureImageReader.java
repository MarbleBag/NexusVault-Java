package nexusvault.format.tex.unc;

/**
 * Thread-Safe
 */
public final class ARGB8888TextureImageReader extends UncompressedTextureImageReader {

	public ARGB8888TextureImageReader() {
		super(new ARGB8888ImageDecoder(), new UncompressedImageMetaCalculator(4));
	}

}
