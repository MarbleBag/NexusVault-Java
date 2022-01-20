package nexusvault.test.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import nexusvault.vault.index.PackedIndexFile;
import nexusvault.vault.pack.PackedFile;

class PackedFileTest {

	private static final Path PACKED_FILE = Path.of("");
	private static final Path PROJECT_DIR = new File("src/test").toPath();

	@Test
	void testOpenPackedFile() throws IOException {
		final var packedFile = new PackedFile();
		packedFile.open(PACKED_FILE);
	}

	@Test
	void testOpenPackedIndexFile() throws IOException {
		final var packedFile = new PackedIndexFile();
		packedFile.openFile(PACKED_FILE);
		final var root = packedFile.getRoot();
		System.out.println("Names: " + root.getChilds().stream().map(e -> e.getName()).collect(Collectors.joining(", ")));
	}

	void testNewPackedIndexFile() throws IOException {
		final var packedFile = new PackedIndexFile(PROJECT_DIR.resolve("resources/IndexFile.index"));
		final var root = packedFile.getRoot();
		root.newDirectory("Dev");
		root.newFile("Off", 0, 0, 0, 0, null, 0);
		root.delete("Dev");
	}
}
