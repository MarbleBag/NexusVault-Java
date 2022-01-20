package nexusvault.export.m3.gltf;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @see PathTextureResource
 */
public abstract class TextureResource {

	TextureResource() {

	}

	public abstract Path writeImageTo(Path outputDirectory) throws IOException;
}