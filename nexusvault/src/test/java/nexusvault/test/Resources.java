package nexusvault.test;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import kreed.io.util.BinaryReader;

public final class Resources {
	private Resources() {
	}

	public static Path path(Path... file) {
		var path = Constants.RESOURCE_DIRECTORY;
		for (final Path element : file) {
			path = path.resolve(element);
		}
		return path;
	}

	public static BinaryReader getFileFromResources(Path file) throws IOException {
		final var fileContent = Files.readAllBytes(Constants.RESOURCE_DIRECTORY.resolve(file));
		return new kreed.io.util.ByteArrayBinaryReader(fileContent, ByteOrder.LITTLE_ENDIAN);
	}

}
