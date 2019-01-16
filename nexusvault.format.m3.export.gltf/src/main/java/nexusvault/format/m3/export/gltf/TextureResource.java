package nexusvault.format.m3.export.gltf;

import java.io.IOException;
import java.nio.file.Path;

import de.javagl.jgltf.impl.v2.Image;

/**
 *
 * @see PathTextureResource
 */
public abstract class TextureResource {
	static enum TextureType {
		DIFFUSE,
		NORMAL
	}

	TextureResource() {

	}

	abstract Image writeImageTo(Path outputDirectory, String outputFileName) throws IOException;
}