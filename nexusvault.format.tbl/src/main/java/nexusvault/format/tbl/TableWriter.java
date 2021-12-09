package nexusvault.format.tbl;

import java.nio.ByteBuffer;
import java.util.HashMap;

import kreed.io.util.BinaryWriter;
import kreed.io.util.ByteAlignmentUtil;
import kreed.io.util.Seek;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.tbl.struct.StructColumnData;
import nexusvault.format.tbl.struct.StructTableFileHeader;
import nexusvault.shared.util.Text;

public final class TableWriter {

	public void write(Table table, BinaryWriter writer) {
		final long writeStartPosition = writer.getPosition();
		writer.seek(Seek.CURRENT, StructTableFileHeader.SIZE_IN_BYTES);
		final long writePostHeaderPosition = writer.getPosition();

		final var header = new StructTableFileHeader();
		header.signature = StructTableFileHeader.SIGNATURE;
		header.version = 0;
		header.nameLength = table.name.length() + 1;
		header.unk1 = table.unk1;

		Text.writeNullTerminatedUTF16(writer, table.name);

		{
			ByteAlignmentUtil.alignTo16Byte(writer);
			header.fieldCount = table.columns.length;
			header.fieldOffset = writer.getPosition() - writePostHeaderPosition;

			long nameOffset = 0;
			final var columnData = new StructColumnData();
			for (final Column column : table.columns) {
				columnData.nameLength = column.name.length() + 1; // null terminated
				columnData.unk1 = column.unk1;
				columnData.nameOffset = nameOffset;
				columnData.dataType = column.dataType.value;
				columnData.unk2 = column.unk2;
				StructUtil.writeStruct(columnData, writer, true);

				nameOffset += ByteAlignmentUtil.alignTo16Byte(columnData.nameLength * 2);
			}

			// ByteAlignmentUtil.alignTo16Byte(writer);
			if (table.columns.length % 2 != 0) {
				writer.seek(Seek.CURRENT, 8);
			}

			for (final Column column : table.columns) {
				Text.writeNullTerminatedUTF16(writer, column.name);
				ByteAlignmentUtil.alignTo16Byte(writer);
			}
		}

		{
			ByteAlignmentUtil.alignTo16Byte(writer);
			header.recordSize = calculateRecordSize(table);
			header.recordCount = table.entries.length;
			header.recordOffset = writer.getPosition() - writePostHeaderPosition;

			final var writeRecordPosition = writer.getPosition();
			var strOffset = header.recordCount * header.recordSize;

			final var stringCache = new HashMap<String, Integer>();

			for (var i = 0; i < header.recordCount; ++i) {
				final var record = table.entries[i];
				for (var j = 0; j < header.fieldCount; ++j) {
					switch (table.columns[j].dataType) {
						case BOOL:
							writer.writeInt32((boolean) record[j] ? 1 : 0);
							break;
						case INT32:
							writer.writeInt32((int) record[j]);
							break;
						case FLOAT:
							writer.writeFloat32((float) record[j]);
							break;
						case INT64:
							writer.writeInt64((long) record[j]);
							break;
						case STRING:
							ByteAlignmentUtil.alignTo8Byte(writer); // padding, all strings are 8 byte aligned.
							final var value = record[j].toString();
							if (stringCache.containsKey(value)) {
								writer.writeInt64(stringCache.get(value).longValue());
							} else {
								writer.writeInt64(strOffset);
								stringCache.put(value, Integer.valueOf((int) strOffset));
								final var position = writer.getPosition();
								writer.seek(Seek.CURRENT, writeRecordPosition + strOffset - position);
								strOffset += Text.writeNullTerminatedUTF16(writer, value);
								writer.seek(Seek.CURRENT, position - writer.getPosition());
							}
							break;
						default:
							throw new TableException(String.format("Unknow data type: '%s'", table.columns[j].dataType));
					}
				}
				ByteAlignmentUtil.alignTo8Byte(writer);
			}

			// number of records * size + all strings
			header.totalRecordsSize = strOffset; // strOffset points to the end of the last written string, so this works.
			writer.seek(Seek.CURRENT, writeRecordPosition + strOffset - writer.getPosition());
		}

		{
			ByteAlignmentUtil.alignTo16Byte(writer);
			header.lookupCount = table.lookup.length;
			header.lookupOffset = writer.getPosition() - writePostHeaderPosition;
			final var byteBuffer = ByteBuffer.allocate(table.lookup.length * 4).order(writer.getOrder());
			byteBuffer.asIntBuffer().put(table.lookup).flip();
			writer.write(byteBuffer);
		}

		{
			final var writeEndOfFile = ByteAlignmentUtil.alignTo16Byte(writer.getPosition());
			final var padding = writeEndOfFile - writer.getPosition();
			for (var i = 0; i < padding; ++i) {
				writer.writeInt8(0);
			}
		}

		writer.seek(Seek.BEGIN, writeStartPosition);
		StructUtil.writeStruct(header, writer, true);
	}

	private long calculateRecordSize(Table table) {
		long size = 0;
		for (final Column column : table.columns) {
			switch (column.dataType) {
				case BOOL:
				case FLOAT:
				case INT32:
					size += 4;
					break;
				case INT64:
					size += 4;
					break;
				case STRING:
					size = ByteAlignmentUtil.alignTo8Byte(size) + 8; // 8 byte aligned pointer.
					break;
				default:
					throw new TableException(String.format("Unknow data type: '%s'", column.dataType));
			}
		}
		return ByteAlignmentUtil.alignTo8Byte(size);
	}

}
