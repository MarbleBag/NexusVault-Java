package nexusvault.format.tex;

public enum TextureFormat {
	UNKNOWN(-1),
	CUSTOM_COMPRESSION(0),
	ARGB(1),
	RGB(5),
	GRAYSCALE(6),
	DXT1(13),
	DXT3(14),
	DXT5(15);

	private int type;

	private TextureFormat(int type) {
		this.type = type;
	}

	public static TextureFormat resolve(int type) {
		for (TextureFormat f : TextureFormat.values()) {
			if (f.type == type)
				return f;
		}
		return UNKNOWN;
	}
}
