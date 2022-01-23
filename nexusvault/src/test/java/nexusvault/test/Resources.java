package nexusvault.test;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

	public static byte[] computeHash(Path file) throws IOException {
		try {
			final var md = MessageDigest.getInstance("MD5");
			try (var is = Files.newInputStream(file); DigestInputStream dis = new DigestInputStream(is, md)) {
				while (dis.read() != -1) {
				}
			}
			return md.digest();
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

}
