package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import nexusvault.vault.index.IndexException.IndexNameCollisionException;
import nexusvault.vault.index.PackedIndexFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackedIndexFileTest {
	static Path file = Constants.RESOURCE_OUT_DIRECTORY.resolve("Packed.index");

	@Test
	@Order(1)
	void testCreateIndexFile() throws IOException {
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
	void testMakeDirectoriesInRoot() throws IOException {
		assumeTrue(Files.exists(file), "packed file does not exist");

		final var packedFile = new PackedIndexFile(file);
		final var rootDirectory = packedFile.getRoot();

		final var directoryA = rootDirectory.newDirectory("art");
		assertTrue("art".equals(directoryA.getName()));
		assertTrue(directoryA.getChilds().isEmpty());
		assertTrue(rootDirectory.getChilds().size() == 1);

		final var directoryB = rootDirectory.newDirectory("dev");
		assertTrue("dev".equals(directoryB.getName()));
		assertTrue(directoryB.getChilds().isEmpty());
		assertTrue(rootDirectory.getChilds().size() == 2);

		packedFile.close();

		packedFile.open(file);
		packedFile.validateFile();
	}

	@Test
	@Order(3)
	void testCreateDirectoryWithSameNameInRoot() throws IOException {
		assumeTrue(Files.exists(file), "packed file does not exist");
		final var packedFile = new PackedIndexFile(file);
		final var rootDirectory = packedFile.getRoot();
		assumeTrue(rootDirectory.getChilds().size() == 2);
		assertThrows(IndexNameCollisionException.class, () -> rootDirectory.newDirectory("dev"));
		assertTrue(rootDirectory.getChilds().size() == 2);
		packedFile.close();
	}

	@Test
	@Order(4)
	void testCreateFile() throws IOException {
		assumeTrue(Files.exists(file), "packed file does not exist");
		final var packedFile = new PackedIndexFile(file);
		final var rootDirectory = packedFile.getRoot();
		assumeTrue(rootDirectory.getChilds().size() == 2);
		rootDirectory.newFile("file", 0, 0, 0, 0, new byte[20], 0);
		assertTrue(rootDirectory.getChilds().size() == 3);
		packedFile.close();
	}

}
