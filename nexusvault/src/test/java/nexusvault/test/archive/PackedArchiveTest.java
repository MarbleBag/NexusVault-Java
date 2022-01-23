package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import nexusvault.test.Constants;
import nexusvault.vault.archive.PackedArchiveFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackedArchiveTest {
	static Path file = Constants.RESOURCE_OUT_DIRECTORY.resolve("Packed.archive");

	@Test
	@Order(1)
	void testCreateIndexFile() throws IOException {
		Files.deleteIfExists(file);
		assumeFalse(Files.exists(file), "packed file does exist");

		final var packedFile = new PackedArchiveFile(file);
		packedFile.validateFile();
		assertTrue(packedFile.getNumberOfEntries() == 0, "newly created archive file should be empty");
		packedFile.close();
	}

}
