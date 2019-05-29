package nexusvault.format.tex;

import nexusvault.format.tex.struct.StructTextureFileHeader;

public enum TextureDataType {
	UNKNOWN(null, null, null) {
		@Override
		protected boolean matches(int format, boolean isCompressed, int compressionFormat) {
			return false;
		}
	},
	JPEG_TYPE_1(null, true, 0),
	JPEG_TYPE_2(null, true, 1),
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

	private TextureDataType(Integer format, Boolean compressed, Integer compressionFormat) {
		this.format = format;
		this.compressed = compressed;
		this.compressionFormat = compressionFormat;
	}

	protected boolean matches(int format, boolean isCompressed, int compressionFormat) {
		final boolean formatMatch = (this.format == null) || ((this.format != null) && (this.format == format));
		final boolean compressionMatch = (compressed == null) || ((compressed != null) && (compressed == isCompressed));
		final boolean compFormatMatch = (this.compressionFormat == null) || ((this.compressionFormat != null) && (this.compressionFormat == compressionFormat));
		return formatMatch && compressionMatch && compFormatMatch;
	}

	public static TextureDataType resolve(StructTextureFileHeader header) {
		return resolve(header.format, header.isCompressed, header.compressionFormat);
	}

	private static TextureDataType resolve(int format, boolean isCompressed, int compressionFormat) {
		for (final TextureDataType f : TextureDataType.values()) {
			if (f.matches(format, isCompressed, compressionFormat)) {
				return f;
			}
		}
		return UNKNOWN;
	}

}
