package nexusvault.test.tbl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

import kreed.io.util.SeekableByteChannelBinaryWriter;
import nexusvault.export.tbl.csv.CsvComplete;
import nexusvault.format.tbl.Table;
import nexusvault.format.tbl.TableReader;
import nexusvault.format.tbl.TableWriter;
import nexusvault.test.Resources;

public class ReadAndWrite {

	private static final Path TBL_RESOURCES = Paths.get("tbl", "org");

	public static Path getTblPath(Path fileName) {
		return Resources.path(TBL_RESOURCES.resolve(fileName));
	}

	@Test
	void test_ReadTbl() throws IOException {
		final var tbl = TableReader.read(Resources.getFileFromResources(TBL_RESOURCES.resolve("AccountCurrencyType.tbl")));
		try (var file = Files.newByteChannel(Resources.path(TBL_RESOURCES.resolve("AccountCurrencyType.out.tbl")), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			try (final var binaryWriter = new SeekableByteChannelBinaryWriter(file, ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN))) {
				TableWriter.write(tbl, binaryWriter);
			}
		}
	}

	@Test
	void test_tbl2csv() throws IOException {
		final var tbl = TableReader.read(Resources.getFileFromResources(TBL_RESOURCES.resolve("AccountCurrencyType.tbl")));
		try (var writer = Files.newBufferedWriter(Resources.path(TBL_RESOURCES.resolve("AccountCurrencyType.csv")), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			final var csvExAndImporter = new CsvComplete();
			csvExAndImporter.write(tbl, writer);
		}
	}

	@Test
	void test_csv2tbl() throws IOException {
		final var originalTbl = TableReader.read(Resources.getFileFromResources(TBL_RESOURCES.resolve("AccountCurrencyType.tbl")));
		Table csvTbl;
		try (var reader = Files.newBufferedReader(Resources.path(TBL_RESOURCES.resolve("AccountCurrencyType.csv")))) {
			final var csvExAndImporter = new CsvComplete();
			csvTbl = csvExAndImporter.read(reader);
		}
		assertEquals(originalTbl, csvTbl);
	}

}
