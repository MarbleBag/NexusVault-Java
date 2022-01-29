package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import nexusvault.test.Constants;
import nexusvault.test.Resources;
import nexusvault.vault.pack.PackException;
import nexusvault.vault.pack.PackException.PackIndexInvalidException;
import nexusvault.vault.pack.PackedFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackedFileTest {

	private static final Path filePath = Constants.RESOURCE_OUT_DIRECTORY.resolve("Packed");

	@BeforeEach
	void cleanup() throws IOException {
		Files.deleteIfExists(filePath);
	}

	@Test
	@Order(1)
	@DisplayName("Create & Save & Read empty file")
	void testCreateNewAndReadNewPackedFile() throws IOException {
		final var packedFile = new PackedFile();
		packedFile.open(filePath);
		packedFile.validateFile();
		assertTrue(packedFile.getEntries().isEmpty(), "newly created packed file should be empty");
		packedFile.close();

		assertTrue(Files.exists(filePath));

		packedFile.open(filePath);
		packedFile.validateFile();
		assertTrue(packedFile.getEntries().isEmpty(), "newly created packed file should be empty");
		packedFile.close();
	}

	@Nested
	@DisplayName("Reading existing file")
	class Reading {

		@BeforeEach
		void setup() throws IOException {
			Files.deleteIfExists(filePath);
			final var packedFile = new PackedFile();
			packedFile.open(filePath);
			packedFile.close();
		}

		@Test
		@Order(1)
		@DisplayName("Index table is inaccessible")
		void test1() throws PackException, IOException {
			final var packedFile = new PackedFile();
			packedFile.open(filePath);

			assertTrue(packedFile.getEntries().isEmpty(), "newly created packed file should be empty");
			assertThrows(PackIndexInvalidException.class, () -> packedFile.writeEntry(1), "write entry did not throw");
			assertThrows(PackIndexInvalidException.class, () -> packedFile.releaseEntry(1), "release entry did not throw");
			assertThrows(PackIndexInvalidException.class, () -> packedFile.claimEntry(1), "claim entry did not throw");
			assertThrows(PackIndexInvalidException.class, () -> packedFile.deleteEntry(1), "delete entry did not throw");
			assertTrue(packedFile.entryCapacity(1) > 0, "table capaciy should be greater than 0");
			assertTrue(packedFile.entrySize(1) == 32, "table size should be equal to 32");

			packedFile.close();
		}

		@Test
		@Order(2)
		@DisplayName("Read does not modify")
		void test2() throws IOException {
			final var originalAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
			final var originalHash = Resources.computeHash(filePath);

			final var packedFile = new PackedFile();
			packedFile.open(filePath);
			packedFile.validateFile();
			packedFile.close();

			final var afterAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
			final var afterHash = Resources.computeHash(filePath);

			assertEquals(originalAttributes.size(), afterAttributes.size(), "Opening an existing file should not modify it");
			assertTrue(originalAttributes.lastModifiedTime().equals(afterAttributes.lastModifiedTime()), "Opening an existing file should not modify it");
			assertArrayEquals(originalHash, afterHash);
		}

	}

}
