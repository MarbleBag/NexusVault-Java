package nexusvault.format.tex;

import java.util.Arrays;

public final class TextureChannel {

	public final TextureChannelFormat format;
	/**
	 * @see TextureChannelType
	 */
	public final TextureChannelType type;
	public final byte[] data;

	public TextureChannel(TextureChannelFormat format, TextureChannelType type, byte[] data) {
		if (format == null) {
			throw new IllegalArgumentException("'format' must not be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("'type' must not be null");
		}
		if (data == null) {
			throw new IllegalArgumentException("'data' must not be null");
		}

		this.format = format;
		this.type = type;
		this.data = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(data);
		result = (prime * result) + ((format == null) ? 0 : format.hashCode());
		result = (prime * result) + ((type == null) ? 0 : type.hashCode());
		return result;
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
		final TextureChannel other = (TextureChannel) obj;
		if (!Arrays.equals(data, other.data)) {
			return false;
		}
		if (format != other.format) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TextureChannel [format=");
		builder.append(format);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

}
