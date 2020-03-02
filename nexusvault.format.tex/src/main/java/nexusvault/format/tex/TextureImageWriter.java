package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.util.Set;

public interface TextureImageWriter {
	public ByteBuffer writeTexture(TexType target, TextureImage[] images);

	Set<TexType> getAcceptedTexTypes();
}
