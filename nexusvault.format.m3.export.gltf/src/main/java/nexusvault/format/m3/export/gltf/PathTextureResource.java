package nexusvault.format.m3.export.gltf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
	public Path writeImageTo(Path outputDirectory) throws IOException {
		final var dst = outputDirectory.resolve(this.source.getFileName());
		Files.copy(this.source, dst, StandardCopyOption.REPLACE_EXISTING);
		final var relativePath = dst.subpath(outputDirectory.getNameCount() - 1, dst.getNameCount());
		return relativePath;
	}
}