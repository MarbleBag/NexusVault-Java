package nexusvault.format.tbl;

import java.nio.ByteBuffer;
import java.util.HashMap;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.tbl.struct.StructColumnData;
import nexusvault.format.tbl.struct.StructTableFileHeader;
import nexusvault.shared.util.Text;

public final class TableReader {

	public Table read(ByteBuffer buffer) {
		return read(new ByteBufferBinaryReader(buffer));
	}

	public Table read(BinaryReader reader) {
		final var header = new StructTableFileHeader(reader);

		// all offsets are from this location
		final long readPostHeaderPosition = reader.getPosition();

		// name is null terminate, ignore it
		final var tableName = Text.readUTF16(reader, (header.nameLength - 1) * 2);

		reader.seek(Seek.BEGIN, readPostHeaderPosition + header.fieldOffset);
		final var fields = new StructColumnData[(int) header.fieldCount];
		for (int i = 0; i < fields.length; ++i) {
			fields[i] = new StructColumnData(reader);
		}

		// ByteAlignmentUtil.alignTo16Byte(reader);
		if (fields.length % 2 != 0) {
			reader.seek(Seek.CURRENT, 8);
		}

		final long readPostFieldsPosition = reader.getPosition();

		final var fieldNames = new String[fields.length];
		for (int i = 0; i < fieldNames.length; ++i) {
			reader.seek(Seek.BEGIN, readPostFieldsPosition + fields[i].nameOffset);
			// name is null terminate, ignore it
			fieldNames[i] = Text.readUTF16(reader, (fields[i].nameLength - 1) * 2);
		}

		final var columns = new Column[fields.length];
		for (var i = 0; i < columns.length; ++i) {
			columns[i] = new Column(fieldNames[i], fields[i].getDataType(), fields[i].unk1, fields[i].unk2);
		}

		final var entries = new Object[(int) header.recordCount][];

		final var stringCache = new HashMap<Integer, String>();

		final long readRecordsPosition = readPostHeaderPosition + header.recordOffset;
		for (var i = 0; i < header.recordCount; ++i) {
			reader.seek(Seek.BEGIN, readRecordsPosition + i * header.recordSize);

			final var record = new Object[fields.length];
			entries[i] = record;

			for (var j = 0; j < columns.length; ++j) {
				switch (columns[j].dataType) {
					case INT32:
						record[j] = reader.readInt32();
						break;
					case FLOAT:
						record[j] = reader.readFloat32();
						break;
					case BOOL:
						final int val = reader.readInt32();
						record[j] = val != 0;
						break;
					case INT64:
						record[j] = reader.readInt64();
						break;
					case STRING:
						if (reader.getPosition() % 8 != 0) {
							reader.readInt32(); // padding, all strings are 8 byte aligned.
						}
						final long strOffset = reader.readInt64();
						if (stringCache.containsKey(Integer.valueOf((int) strOffset))) {
							record[j] = stringCache.get(Integer.valueOf((int) strOffset));
						} else {
							final long readPosition = reader.getPosition();
							reader.seek(Seek.BEGIN, readRecordsPosition + strOffset);
							record[j] = Text.readNullTerminatedUTF16(reader);
							reader.seek(Seek.BEGIN, readPosition);
							stringCache.put(Integer.valueOf((int) strOffset), (String) record[j]);
						}
						break;
					default:
						throw new TableException(String.format("Unknow data type: '%s'", columns[j].dataType));
				}
			}
		}

		reader.seek(Seek.BEGIN, readPostHeaderPosition + header.lookupOffset);
		final var lookup = new int[(int) header.lookupCount];
		{
			final var lookupByteArray = new byte[lookup.length * 4];
			reader.readInt8(lookupByteArray, 0, lookupByteArray.length);
			ByteBuffer.wrap(lookupByteArray).order(reader.getOrder()).asIntBuffer().get(lookup);
		}

		return new Table(tableName, columns, entries, lookup, header.unk1);
	}

}
