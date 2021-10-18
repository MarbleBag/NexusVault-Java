package nexusvault.format.tbl.better;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.tbl.TextUtil;
import nexusvault.format.tbl.struct.StructColumnData;
import nexusvault.format.tbl.struct.StructTableFileHeader;

public class TableReader {

	public UntypedTable read(BinaryReader reader) {

		final long startPosition = reader.getPosition();

		final var header = new StructTableFileHeader(reader);

		// all offsets are from this location
		final long postHeaderPosition = reader.getPosition();

		// name is null terminate, but we don't need that part in Java
		final var tableName = TextUtil.extractUTF16(reader, (header.nameLength - 1) * 2);

		reader.seek(Seek.BEGIN, postHeaderPosition + header.fieldOffset);
		final var fields = new StructColumnData[(int) header.fieldCount];
		for (int i = 0; i < fields.length; ++i) {
			fields[i] = new StructColumnData(reader);
		}

		// each field is 24 bytes, but blocks are aligned to 16 byte. Therefore, for an odd number of field, skip 8 bytes!
		if (fields.length % 2 != 0) {
			reader.seek(Seek.CURRENT, 8);
		}

		final long postFieldsPosition = reader.getPosition();

		final var fieldNames = new String[fields.length];
		for (int i = 0; i < fieldNames.length; ++i) {
			reader.seek(Seek.BEGIN, postFieldsPosition + fields[i].nameOffset);
			// name is null terminate, but we don't need that part in Java
			fieldNames[i] = TextUtil.extractUTF16(reader, (fields[i].nameLength - 1) * 2);
		}

		final var columns = new Column[fields.length];
		for (var i = 0; i < columns.length; ++i) {
			columns[i] = new Column(fieldNames[i], fields[i].getDataType(), fields[i].unk1, fields[i].unk2);
		}

		final var records = new ArrayList<Object[]>((int) header.recordSize);

		final long recordsPosition = postHeaderPosition + header.recordOffset;
		for (var i = 0; i < header.recordCount; ++i) {
			reader.seek(Seek.BEGIN, recordsPosition + i * header.recordSize);

			final var record = new Object[fields.length]; // TODO
			records.add(record);

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

						final long offset = reader.readInt64();
						final long lastPosition = reader.getPosition();
						final long dataOffset = recordsPosition + offset;

						reader.seek(Seek.BEGIN, dataOffset);
						record[j] = TextUtil.extractNullTerminatedUTF16(reader);
						reader.seek(Seek.BEGIN, lastPosition);
						break;
					default:
						throw new IllegalArgumentException(String.format("Unknow data type: '%s'", columns[j].dataType));
				}
			}

		}

		reader.seek(Seek.BEGIN, postHeaderPosition + header.lookupOffset);
		final var lookup = new int[(int) header.lookupCount];
		{
			final var lookupByteArray = new byte[lookup.length * 4];
			reader.readInt8(lookupByteArray, 0, lookupByteArray.length);
			ByteBuffer.wrap(lookupByteArray).order(reader.getOrder()).asIntBuffer().get(lookup);
		}

		return new UntypedTable(tableName, columns, records, lookup);
	}

}
