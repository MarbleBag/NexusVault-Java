package nexusvault.format.tex;

public enum TextureImageFormat{
	RGB(3),
	ARGB(4),
	GRAYSCALE(1);

	private final int bytePerPixel;

	private TextureImageFormat(int bytePerPixel) {
		this.bytePerPixel = bytePerPixel;
	}

	public int getBytesPerPixel() {
		return this.bytePerPixel;
	}
}
