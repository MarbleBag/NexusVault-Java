package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructFileHeader;

public enum TextureType {
	UNKNOWN(-1, false, -1) {
		@Override
		protected boolean matches(int format, boolean isCompressed, int compressionFormat) {
			return false;
		}
	},
	/**
	 * Chroma subsampling &amp; typical jpg color space transformation with one additional color channel
	 */
	JPG1(0, true, 0),
	/**
	 * Four color channels and no color space transformation
	 */
	JPG2(0, true, 1),
	/**
	 * typical jpg color space transformation with one additional color channel
	 */
	JPG3(0, true, 2),
	/** identical to {@link #ARGB2} */
	ARGB1(0, false, 0),
	/** identical to {@link #ARGB1} */
	ARGB2(1, false, 0),
	RGB(5, false, 0),
	GRAYSCALE(6, false, 0),
	DXT1(13, false, 0),
	DXT3(14, false, 0),
	DXT5(15, false, 0);

	private int format;
	private boolean isJpg;
	private int jpgFormat;

	private TextureType(int format, boolean isJpg, int jpgFormat) {
		this.format = format;
		this.isJpg = isJpg;
		this.jpgFormat = jpgFormat;
	}

	public int getFormat() {
		return this.format;
	}

	public boolean isJpg() {
		return this.isJpg;
	}

	public int getJpgFormat() {
		return this.jpgFormat;
	}

	protected boolean matches(int format, boolean isJpg, int jpgFormat) {
		if (this.isJpg) {
			return isJpg && jpgFormat == this.jpgFormat;
		} else {
			return !isJpg && this.format == format;
		}
	}

	public static TextureType resolve(StructFileHeader header) {
		return resolve(header.format, header.isJpg, header.jpgFormat);
	}

	public static TextureType resolve(String name) {
		name = name.toUpperCase();
		for (final TextureType f : TextureType.values()) {
			if (f.name().toUpperCase().equals(name)) {
				return f;
			}
		}
		return UNKNOWN;
	}

	public static TextureType resolve(int format, boolean isJpg, int jpgFormat) {
		for (final TextureType f : TextureType.values()) {
			if (f.matches(format, isJpg, jpgFormat)) {
				return f;
			}
		}
		return UNKNOWN;
	}

}
