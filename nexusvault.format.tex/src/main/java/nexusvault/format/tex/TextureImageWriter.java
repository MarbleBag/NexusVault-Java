package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public interface TextureImageWriter {

	static final String CONFIG_DEPTH = "tex.depth";
	static final String CONFIG_SIDES = "tex.sides";

	ByteBuffer writeTexture(TexType target, TextureImage[] images, Map<String, Object> config);

	Set<TexType> getAcceptedTexTypes();
}
