package nexusvault.test.tbl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nexusvault.format.tbl.Column;
import nexusvault.format.tbl.ColumnType;
import nexusvault.format.tbl.Table;
import nexusvault.format.tbl.TableLookup;
import nexusvault.format.tbl.TableReader;
import nexusvault.format.tbl.TableWriter;

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

		final var table = new Table("Test of animals", columns, entries, lookup);

		final var binary = TableWriter.toBinary(table);

		final var createdTable = TableReader.read(binary);

		assertEquals(table, createdTable);
	}

}
