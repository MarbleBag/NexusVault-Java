package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public interface TextureImageWriter {

	ByteBuffer writeTexture(TexType target, TextureImage[] images, Map<String, Object> config);

	Set<TexType> getAcceptedTexTypes();
}
