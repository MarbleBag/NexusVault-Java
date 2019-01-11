package nexusvault.format.tex.jpg;

import nexusvault.format.tex.TextureChannelFormat;
import nexusvault.format.tex.TextureChannelType;

public final class TextureChannelInfo {
	private final TextureChannelFormat format;
	private final TextureChannelType type;

	public TextureChannelInfo(TextureChannelFormat format, TextureChannelType type) {
		super();
		this.format = format;
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TextureChannelInfo other = (TextureChannelInfo) obj;
		if (format != other.format) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	public TextureChannelFormat getFormat() {
		return format;
	}

	public TextureChannelType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((format == null) ? 0 : format.hashCode());
		result = (prime * result) + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TextureChannelInfo [format=");
		builder.append(format);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

}
