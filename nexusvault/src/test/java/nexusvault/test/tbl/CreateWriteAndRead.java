package nexusvault.test.tbl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import nexusvault.format.tbl.Column;
import nexusvault.format.tbl.ColumnType;
import nexusvault.format.tbl.Table;
import nexusvault.format.tbl.TableLookup;
import nexusvault.format.tbl.TableReader;
import nexusvault.format.tbl.TableWriter;
import nexusvault.test.Constants;

class CreateWriteAndRead {

	@Test
	public void testCreateTable() {
		final var columns = new Column[] { //
				new Column("id", ColumnType.INT32, 0), //
				new Column("Comment", ColumnType.STRING, 0), //
				new Column("Animal", ColumnType.STRING, 0), //
				new Column("Animalness", ColumnType.INT32, 0) //
		};

		final var entries = new Object[][] { //
				new Object[] { 1, "First Entry", "Bird", 25 } //
		};

		final var lookup = TableLookup.sortEntriesAndComputeLookup(entries);
		final var originalTable = new Table("Test of animals", columns, entries, lookup);
		final var binary = TableWriter.toBinary(originalTable);
		final var recreatedTable = TableReader.read(binary);

		assertEquals(originalTable, recreatedTable);
	}

	@Test
	public void testRead() throws IOException {
		final var filePath = Constants.RESOURCE_IN_DIRECTORY.resolve("Table.tbl");
		assumeTrue(Files.exists(filePath));
		final var table = TableReader.read(Files.readAllBytes(filePath));
		assertEquals(23, table.columns.length);
		assertEquals(4943, table.entries.length);
	}

	@Test
	public void testWrite() throws IOException {
		final var filePath = Constants.RESOURCE_IN_DIRECTORY.resolve("Table.tbl");
		assumeTrue(Files.exists(filePath));
		final var originalBytes = Files.readAllBytes(filePath);
		final var table = TableReader.read(originalBytes);
		final var recreatedBytes = TableWriter.toBinary(table);
		assertArrayEquals(originalBytes, recreatedBytes);
	}

}
