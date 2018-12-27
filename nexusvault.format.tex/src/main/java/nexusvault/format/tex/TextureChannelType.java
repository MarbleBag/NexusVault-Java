package nexusvault.format.tex;

public enum TextureChannelType {
	RGB(3),
	ARGB(4),
	GRAYSCALE(1);

	private final int bytePerPixel;

	private TextureChannelType(int bytePerPixel) {
		this.bytePerPixel = bytePerPixel;
	}

	public int getBytesPerPixel() {
		return this.bytePerPixel;
	}
}
