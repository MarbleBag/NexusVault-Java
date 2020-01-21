package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructTextureFileHeader;

public enum TexType {
	UNKNOWN(null, null, null) {
		@Override
		protected boolean matches(int format, boolean isCompressed, int compressionFormat) {
			return false;
		}
	},
	/**
	 * Chroma subsampling & typical jpg color space transformation with one additional color channel
	 */
	JPEG_TYPE_1(null, true, 0),
	/**
	 * Four color channels and no color space transformation
	 */
	JPEG_TYPE_2(null, true, 1),
	/**
	 * typical jpg color space transformation with one additional color channel
	 */
	JPEG_TYPE_3(null, true, 2),
	/** identical to {@link #ARGB_2} */
	ARGB_1(0, false, null),
	/** identical to {@link #ARGB_1} */
	ARGB_2(1, false, null),
	RGB(5, false, null),
	GRAYSCALE(6, false, null),
	DXT1(13, false, null),
	DXT3(14, false, null),
	DXT5(15, false, null);

	private Integer format;
	private Boolean compressed;
	private Integer compressionFormat;

	private TexType(Integer format, Boolean compressed, Integer compressionFormat) {
		this.format = format;
		this.compressed = compressed;
		this.compressionFormat = compressionFormat;
	}

	public int getFormat() {
		return format == 0 ? 0 : format.intValue();
	}

	public boolean isCompressed() {
		return compressed == null ? false : compressed.booleanValue();
	}

	public int getCompressionFormat() {
		return compressionFormat == 0 ? 0 : compressionFormat.intValue();
	}

	protected boolean matches(int format, boolean isCompressed, int compressionFormat) {
		final boolean formatMatch = (this.format == null) || ((this.format != null) && (this.format == format));
		final boolean compressionMatch = (compressed == null) || ((compressed != null) && (compressed == isCompressed));
		final boolean compFormatMatch = (this.compressionFormat == null) || ((this.compressionFormat != null) && (this.compressionFormat == compressionFormat));
		return formatMatch && compressionMatch && compFormatMatch;
	}

	public static TexType resolve(StructTextureFileHeader header) {
		return resolve(header.format, header.isCompressed, header.compressionFormat);
	}

	private static TexType resolve(int format, boolean isCompressed, int compressionFormat) {
		for (final TexType f : TexType.values()) {
			if (f.matches(format, isCompressed, compressionFormat)) {
				return f;
			}
		}
		return UNKNOWN;
	}

}
