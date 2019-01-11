package nexusvault.format.tex;

final class TextureSplitInfo {
	public final TextureImageFormat format;
	public final TextureImageExpectedUseType type;

	public TextureSplitInfo(TextureImageFormat format, TextureImageExpectedUseType type) {
		super();
		this.format = format;
		this.type = type;
	}

	public TextureImageFormat getFormat() {
		return format;
	}

	public TextureImageExpectedUseType getType() {
		return type;
	}

}