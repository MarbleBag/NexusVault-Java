package nexusvault.format.m3.export.gltf;

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