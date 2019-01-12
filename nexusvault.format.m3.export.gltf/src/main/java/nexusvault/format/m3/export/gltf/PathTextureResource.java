package nexusvault.format.m3.export.gltf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import de.javagl.jgltf.impl.v2.Image;

// TODO
public final class PathTextureResource extends TextureResource {
	private final Path source;

	public PathTextureResource(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("'path' must not be null");
		}
		this.source = path;
	}

	@Override
	Image writeImageTo(Path outputDirectory, String outputFileName) throws IOException {
		final Path dst = outputDirectory.resolve(source.getFileName());
		Files.copy(source, dst, StandardCopyOption.REPLACE_EXISTING);
		final Image image = new Image(); // TODO
		image.setUri(dst.getFileName().toString());
		return image;
	}
}