package nexusvault.format.tex;

import java.nio.ByteBuffer;

public interface TextureImageWriter {
	public ByteBuffer writeTexture(TexType target, TextureImage[] images);
}
