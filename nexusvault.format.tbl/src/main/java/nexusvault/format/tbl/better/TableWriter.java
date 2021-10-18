package nexusvault.format.tbl.better;

import kreed.io.util.BinaryWriter;
import kreed.io.util.Seek;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.tbl.TextUtil;
import nexusvault.format.tbl.struct.StructColumnData;
import nexusvault.format.tbl.struct.StructTableFileHeader;
import nexusvault.shared.util.ByteAlign;

public class TableWriter {

	public <T> void write(XTable<T> table, BinaryWriter writer) {
		final var header = buildHeader(table);
		StructUtil.writeStruct(header, writer, true);
		final long postHeaderPosition = writer.getPosition();

		TextUtil.writeNullTerminatedUTF16(writer, table.getName());

		writer.seek(Seek.CURRENT, postHeaderPosition + header.fieldOffset - writer.getPosition());
		// TODO

		writer.seek(Seek.CURRENT, postHeaderPosition + header.recordOffset - writer.getPosition());
		// TODO

		writer.seek(Seek.CURRENT, postHeaderPosition + header.lookupOffset - writer.getPosition());
		// TODO

		// TODO
		// Write Header
		// Write Name
		// Pad to 16
		// Write column
		// Pad to 16
		// Write column names
		// Pad each name to 16
		// Write records, look out for strings, each must be padded to 8
		// Write any string data
		// Pad to 16
		// Write lookup
		// Pad to 16
		// Update header, write header new?

		// final var output = ByteBuffer.allocate(StructTextureFileHeader.SIZE_IN_BYTES + imageSizes).order(ByteOrder.LITTLE_ENDIAN);
		// kreed.reflection.struct.StructUtil.writeStruct(header, writer, true);

	}

	protected <T> StructTableFileHeader buildHeader(XTable<T> table) {
		final var header = new StructTableFileHeader();
		header.signature = StructTableFileHeader.SIGNATURE;
		header.version = 0;
		header.nameLength = table.getName().length() * 2 + 2; // length in byte for UTF16 + Null
		header.unk1 = table.unk1;

		// all offsets start right after the header.

		header.fieldCount = table.columns.length;
		header.fieldOffset = ByteAlign.alignTo16Byte(header.nameLength); // right after the table name block

		header.recordSize = calculateRecordSize(table);
		header.recordCount = table.entries.size();
		header.recordOffset = ByteAlign.alignTo16Byte(calculateColumnSize(table));
		header.totalRecordSize = ByteAlign.alignTo16Byte(header.recordSize * header.recordCount);

		header.lookupCount = table.lookup.length;
		header.lookupOffset = ByteAlign.alignTo16Byte(header.recordOffset + header.totalRecordSize);

		return header;
	}

	private <T> long calculateRecordSize(XTable<T> table) {
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
					size = ByteAlign.alignTo8Byte(size) + 8; // 8 byte aligned pointer.
					break;
				default:
					throw new IllegalArgumentException(String.format("Unknow data type: '%s'", column.dataType));
			}
		}
		return ByteAlign.alignTo8Byte(size);
	}

	private <T> long calculateColumnSize(XTable<T> table) {
		// number of columns times the size aligned to 16
		long size = ByteAlign.alignTo16Byte(table.columns.length * StructColumnData.SIZE_IN_BYTES);

		// each column name is stored as UTF16 and null terminated, also, each name is aligned to 16!
		for (final Column column : table.columns) {
			size += ByteAlign.alignTo16Byte(column.name.length() * 2 + 2);
		}

		return size;
	}

}
