package nexusvault.format.tex;

public enum TextureImageFormat {
	/** 3 bytes per pixel */
	RGB(3),
	/** 4 bytes per pixel */
	ARGB(4),
	/** 1 bytes per pixel */
	GRAYSCALE(1);

	private final int bytePerPixel;

	private TextureImageFormat(int bytePerPixel) {
		this.bytePerPixel = bytePerPixel;
	}

	public int getBytesPerPixel() {
		return bytePerPixel;
	}
}
