package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import nexusvault.test.Constants;
import nexusvault.vault.index.PackedIndexFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackedIndexFileTest {
	static Path file = Constants.RESOURCE_OUT_DIRECTORY.resolve("Packed.index");

	@Test
	@Order(1)
	void testCreateFile() throws IOException {
		Files.deleteIfExists(file);
		assumeFalse(Files.exists(file), "packed file does exist");

		final var packedFile = new PackedIndexFile(file);
		packedFile.validateFile();
		assertNotNull(packedFile.getRoot());
		assertTrue(packedFile.getRoot().getChilds().isEmpty(), "newly created index file should be empty");
		packedFile.close();
	}

	@Test
	@Order(2)
	void testMakeDirectories() throws IOException {
		assumeTrue(Files.exists(file), "packed file does not exist");

		final var packedFile = new PackedIndexFile(file);
		final var rootDirectory = packedFile.getRoot();

		assertAll(() -> {
			final var createdDirectory = rootDirectory.newDirectory("art");
			assertTrue("art".equals(createdDirectory.getName()));
			assertTrue(createdDirectory.getChilds().isEmpty());
			assertTrue(rootDirectory.getChilds().size() == 1);
		}, () -> {
			final var createdDirectory = rootDirectory.newDirectory("dev");
			assertTrue("dev".equals(createdDirectory.getName()));
			assertTrue(createdDirectory.getChilds().isEmpty());
			assertTrue(rootDirectory.getChilds().size() == 2);
		});

		packedFile.close();
	}
}
