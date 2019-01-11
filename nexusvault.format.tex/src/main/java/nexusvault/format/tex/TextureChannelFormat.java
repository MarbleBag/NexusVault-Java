package nexusvault.format.tex;

public enum TextureChannelFormat {
	RGB(3),
	ARGB(4),
	GRAYSCALE(1);

	private final int bytePerPixel;

	private TextureChannelFormat(int bytePerPixel) {
		this.bytePerPixel = bytePerPixel;
	}

	public int getBytesPerPixel() {
		return this.bytePerPixel;
	}
}
