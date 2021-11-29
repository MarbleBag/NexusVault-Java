package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructTextureFileHeader;

public enum TexType {
	UNKNOWN(-1, false, -1) {
		@Override
		protected boolean matches(int format, boolean isCompressed, int compressionFormat) {
			return false;
		}
	},
	/**
	 * Chroma subsampling &amp; typical jpg color space transformation with one additional color channel
	 */
	JPEG_TYPE_1(-1, true, 0),
	/**
	 * Four color channels and no color space transformation
	 */
	JPEG_TYPE_2(-1, true, 1),
	/**
	 * typical jpg color space transformation with one additional color channel
	 */
	JPEG_TYPE_3(-1, true, 2),
	/** identical to {@link #ARGB_2} */
	ARGB_1(0, false, -1),
	/** identical to {@link #ARGB_1} */
	ARGB_2(1, false, -1),
	RGB(5, false, -1),
	GRAYSCALE(6, false, -1),
	DXT1(13, false, -1),
	DXT3(14, false, -1),
	DXT5(15, false, -1);

	private int format;
	private boolean isJpg;
	private int jpgFormat;

	private TexType(int format, boolean isJpg, int jpgFormat) {
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
		if (isJpg == this.isJpg) {
			return jpgFormat == this.jpgFormat;
		}
		return format == this.format;
	}

	public static TexType resolve(StructTextureFileHeader header) {
		return resolve(header.format, header.isJpg, header.jpgFormat);
	}

	public static TexType resolve(String name) {
		name = name.toUpperCase();
		for (final TexType f : TexType.values()) {
			if (f.name().toUpperCase().equals(name)) {
				return f;
			}
		}
		return UNKNOWN;
	}

	public static TexType resolve(int format, boolean isJpg, int jpgFormat) {
		for (final TexType f : TexType.values()) {
			if (f.matches(format, isJpg, jpgFormat)) {
				return f;
			}
		}
		return UNKNOWN;
	}

}
