package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import nexusvault.test.Constants;
import nexusvault.vault.archive.ArchiveException.ArchiveHashCollisionException;
import nexusvault.vault.archive.Hash;
import nexusvault.vault.archive.PackedArchiveFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackedArchiveTest {
	static final Path filePath = Constants.RESOURCE_OUT_DIRECTORY.resolve("Packed.archive");
	static final String text = "Sand polar sand dunes\nA hilarious goat stands\nunder the chicken";

	@BeforeEach
	void cleanup() throws IOException {
		Files.deleteIfExists(filePath);
	}

	@Test
	void testCreateIndexFileA() throws IOException {
		var packedFile = new PackedArchiveFile(filePath);
		packedFile.validateFile();
		assertTrue(packedFile.isOpen());
		assertTrue(packedFile.getNumberOfEntries() == 0, "newly created archive file should be empty");
		packedFile.close();

		packedFile = new PackedArchiveFile(filePath);
		packedFile.validateFile();
		assertTrue(packedFile.getNumberOfEntries() == 0, "newly created archive file should be empty");
		packedFile.close();
	}

	@Test
	void testCreateIndexFileB() throws IOException {
		var packedFile = new PackedArchiveFile(filePath);
		assertTrue(packedFile.isOpen());
		assertTrue(packedFile.getNumberOfEntries() == 0, "newly created archive file should be empty");
		packedFile.close();

		packedFile = new PackedArchiveFile(filePath);
		packedFile.validateFile();
		assertTrue(packedFile.getNumberOfEntries() == 0, "newly created archive file should be empty");
		packedFile.close();
	}

	@Nested
	class EmptyFile {
		PackedArchiveFile file = new PackedArchiveFile();

		@BeforeEach
		void loadFile() throws IOException {
			Files.deleteIfExists(filePath);
			this.file.open(filePath);
		}

		@AfterEach
		void closeFile() throws IOException {
			this.file.close();
		}

		@Test
		void testStoreData() throws IOException {
			final var data = text.getBytes(StandardCharsets.UTF_8);
			final var hash = Hash.computeHash(data);
			this.file.writeData(hash, data, false);

			assertThrows(ArchiveHashCollisionException.class, () -> this.file.writeData(hash, data, false));
			assertTrue(this.file.hasData(hash));
			assertArrayEquals(data, this.file.getData(hash));
			assertEquals(1, this.file.getNumberOfEntries());

			this.file.close();

			this.file.open(filePath);
			this.file.validateFile();

			assertThrows(ArchiveHashCollisionException.class, () -> this.file.writeData(hash, data, false));
			assertTrue(this.file.hasData(hash));
			assertArrayEquals(data, this.file.getData(hash));
			assertEquals(1, this.file.getNumberOfEntries());
		}
	}

}
