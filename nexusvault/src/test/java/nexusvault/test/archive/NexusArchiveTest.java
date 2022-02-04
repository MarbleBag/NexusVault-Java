package nexusvault.test.archive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import nexusvault.test.Constants;
import nexusvault.vault.IdxPath;
import nexusvault.vault.NexusArchive;
import nexusvault.vault.NexusArchive.CompressionType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NexusArchiveTest {

	static final Path indexFilePath = Constants.RESOURCE_OUT_DIRECTORY.resolve("NexusArchive.index");
	static final Path archiveFilePath = indexFilePath.resolveSibling("NexusArchive.archive");

	@BeforeEach
	void cleanupArchives() throws IOException {
		Files.deleteIfExists(indexFilePath);
		Files.deleteIfExists(archiveFilePath);
	}

	@Test
	@Order(1)
	void testCreateNewArchive() throws IOException {
		final var archive = NexusArchive.open(indexFilePath);
		archive.close();

		final var files = archive.getFiles();
		assertNotNull(files);
		assertEquals(indexFilePath, files.getIndexFile());
		assertEquals(archiveFilePath, files.getArchiveFile());
		assertTrue(Files.exists(files.getIndexFile()));
		assertTrue(Files.exists(files.getArchiveFile()));
	}

	@Test
	@Order(2)
	void testWriteAndRead() throws IOException {
		var archive = NexusArchive.open(indexFilePath);
		final var entry = IdxPath.createPath("test", "poem", "Rabbit - A Haiku.txt");
		final var poem = "Chilly break of day\nAn old, gorgeous rabbit roars\nenjoying the cow";
		archive.write(entry, poem.getBytes(StandardCharsets.UTF_8), CompressionType.UNCOMPRESSED);

		{
			final var file = archive.find(entry);
			assertTrue(file.isPresent(), "entry not found");
			assertTrue(file.get().isFile(), "entry is not a file");
			assertEquals("txt", file.get().asFile().getFileEnding());
			final var restoredPoem = new String(file.get().asFile().getData(), StandardCharsets.UTF_8);
			assertEquals(poem, restoredPoem);
		}

		archive.close();

		assertTrue(Files.exists(indexFilePath));
		assertTrue(Files.exists(archiveFilePath));

		archive = NexusArchive.open(indexFilePath);
		archive.validateArchive();

		{
			final var file = archive.find(entry);
			assertTrue(file.isPresent(), "entry not found");
			assertTrue(file.get().isFile(), "entry is not a file");
			assertEquals("txt", file.get().asFile().getFileEnding());
			final var restoredPoem = new String(file.get().asFile().getData(), StandardCharsets.UTF_8);
			assertEquals(poem, restoredPoem);
		}
	}

}
