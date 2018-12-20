package nexusvault.format.tex;

public class TextureChannel {

	public final TextureChannelType type;
	public final byte[] data;

	public TextureChannel(TextureChannelType type, byte[] data) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		if (data == null) {
			throw new IllegalArgumentException();
		}

		this.type = type;
		this.data = data;
	}
}
