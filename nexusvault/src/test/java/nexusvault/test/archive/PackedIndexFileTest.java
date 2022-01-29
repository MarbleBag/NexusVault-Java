package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import nexusvault.test.Constants;
import nexusvault.vault.index.IndexException.IndexNameCollisionException;
import nexusvault.vault.index.PackedIndexFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackedIndexFileTest {

	static final Path filePath = Constants.RESOURCE_OUT_DIRECTORY.resolve("Packed.index");

	@BeforeEach
	void cleanup() throws IOException {
		Files.deleteIfExists(filePath);
		assertFalse(Files.exists(filePath), "packed file does still exist");
	}

	@Test
	@Order(1)
	void testCreateIndexFile() throws IOException {
		final var packedFile = new PackedIndexFile(filePath);
		packedFile.validateFile();
		assertNotNull(packedFile.getRoot());
		assertTrue(packedFile.getRoot().getChilds().isEmpty(), "newly created index file should be empty");
		packedFile.close();

		packedFile.open(filePath);
		packedFile.validateFile();
		packedFile.close();
	}

	@Test
	@Order(2)
	void testMakeDirectoriesInRoot() throws IOException {
		final var packedFile = new PackedIndexFile(filePath);

		final var directoryA = packedFile.getRoot().newDirectory("art");
		assertTrue("art".equals(directoryA.getName()));
		assertTrue(directoryA.getChilds().isEmpty());
		assertTrue(packedFile.getRoot().getChilds().size() == 1);

		final var directoryB = packedFile.getRoot().newDirectory("dev");
		assertTrue("dev".equals(directoryB.getName()));
		assertTrue(directoryB.getChilds().isEmpty());
		assertTrue(packedFile.getRoot().getChilds().size() == 2);

		packedFile.close();

		packedFile.open(filePath);
		packedFile.validateFile();
		assertTrue(packedFile.getRoot().getChilds().size() == 2);
		assertTrue(packedFile.getRoot().hasChild("art"));
		assertTrue(packedFile.getRoot().hasChild("dev"));
		packedFile.close();
	}

	@Test
	@Order(3)
	void testCreateDirectoryWithSameNameInRoot() throws IOException {
		final var packedFile = new PackedIndexFile(filePath);
		packedFile.getRoot().newDirectory("dev");
		assumeTrue(packedFile.getRoot().getChilds().size() == 1);
		assertTrue(packedFile.getRoot().hasChild("dev"));
		assertThrows(IndexNameCollisionException.class, () -> packedFile.getRoot().newDirectory("dev"));
		assertTrue(packedFile.getRoot().getChilds().size() == 1);
		assertTrue(packedFile.getRoot().hasChild("dev"));
		packedFile.close();
	}

	@Test
	@Order(4)
	void testCreateFile() throws IOException {
		final var packedFile = new PackedIndexFile(filePath);
		packedFile.getRoot().newFile("fileName", 0, 0, 0, 0, new byte[20], 0);
		assertTrue(packedFile.getRoot().getChilds().size() == 1);
		packedFile.close();

		packedFile.open(filePath);
		packedFile.validateFile();
		assertTrue(packedFile.getRoot().getChilds().size() == 1);
		assertTrue(packedFile.getRoot().hasChild("fileName"));
		packedFile.close();
	}

}
