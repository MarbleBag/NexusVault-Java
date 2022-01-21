package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import nexusvault.test.Constants;
import nexusvault.vault.pack.PackException;
import nexusvault.vault.pack.PackException.PackIndexInvalidException;
import nexusvault.vault.pack.PackedFile;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PackedFileTest {

	static Path file = Constants.RESOURCE_OUT_DIRECTORY.resolve("Packed");

	@Test
	@Order(1)
	void testCreatePackedFile() throws IOException {
		Files.deleteIfExists(file);
		assumeFalse(Files.exists(file), "packed file does exist");

		final var packedFile = new PackedFile();
		packedFile.open(file);
		packedFile.validateFile();

		assertTrue(packedFile.getEntries().isEmpty(), "newly created packed file should be empty");

		packedFile.close();
	}

	@Test
	@Order(2)
	void testValidateExistingPackedFile() throws IOException {
		assumeTrue(Files.exists(file), "packed file does not exist");
		final var originalAttributes = Files.readAttributes(file, BasicFileAttributes.class);

		final var packedFile = new PackedFile();
		packedFile.open(file);
		packedFile.validateFile();
		packedFile.close();

		final var afterAttributes = Files.readAttributes(file, BasicFileAttributes.class);

		assertEquals(originalAttributes.size(), afterAttributes.size(), "Opening an existing file should not modify it");
		assertTrue(originalAttributes.lastModifiedTime().equals(afterAttributes.lastModifiedTime()), "Opening an existing file should not modify it");
	}

	@Test
	@Order(3)
	void testIndexTableIsInaccessible() throws PackException, IOException {
		assumeTrue(Files.exists(file), "packed file does not exist");

		final var packedFile = new PackedFile();
		packedFile.open(file);

		assertTrue(packedFile.getEntries().isEmpty(), "newly created packed file should be empty");
		assertThrows(PackIndexInvalidException.class, () -> packedFile.writeEntry(1), "write entry did not throw");
		assertThrows(PackIndexInvalidException.class, () -> packedFile.releaseEntry(1), "release entry did not throw");
		assertThrows(PackIndexInvalidException.class, () -> packedFile.claimEntry(1), "claim entry did not throw");
		assertThrows(PackIndexInvalidException.class, () -> packedFile.deleteEntry(1), "delete entry did not throw");
		assertTrue(packedFile.entryCapacity(1) > 0, "table capaciy should be greater than 0");
		assertTrue(packedFile.entrySize(1) == 32, "table size should be equal to 32");

		packedFile.close();
	}

}
