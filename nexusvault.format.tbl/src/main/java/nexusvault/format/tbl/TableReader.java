package nexusvault.format.tbl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;

public final class TableReader {

	/**
	 * Constructs an untyped table from the given input, which can be used in combination with {@link LazyLoadedTypedTable} or {@link PreloadedTypedTable} and a
	 * typed entry to create a typed table.
	 *
	 * @param buffer
	 *            contains the data to process
	 * @return the constructed table from the given input
	 * @see LazyLoadedTypedTable
	 * @see PreloadedTypedTable
	 */
	public RawTable read(ByteBuffer buffer) {
		return read(new ByteBufferBinaryReader(buffer));
	}

	/**
	 * Constructs an untyped table from the given input, which can be used in combination with {@link LazyLoadedTypedTable} or {@link PreloadedTypedTable} and a
	 * typed entry to create a typed table.
	 *
	 * @param reader
	 *            contains the data to process
	 * @return the constructed table from the given input
	 * @see LazyLoadedTypedTable
	 * @see PreloadedTypedTable
	 */
	public RawTable read(BinaryReader reader) {

		final long tableStart = reader.getPosition();
		final long postHeaderPosition = tableStart + StructTableFileHeader.SIZE_IN_BYTES;

		final StructTableFileHeader tableHeader = loadTableHeader(reader);
		final StructTableFieldHeader[] tableFields = loadTableFields(tableHeader, postHeaderPosition, reader);

		final List<TableRecord> records = loadTableRecords(tableHeader, tableFields, postHeaderPosition, reader);
		final int[] lookUps = loadTableLookup(tableHeader, postHeaderPosition, reader);

		final RawTable table = new RawTable(tableHeader, tableFields, records, lookUps);
		return table;
	}

	public int getFileSignature() {
		return StructTableFileHeader.SIGNATURE;
	}

	public boolean acceptFileSignature(int signature) {
		return getFileSignature() == signature;
	}

	public boolean acceptFileVersion(int version) {
		return true;
	}

	protected StructTableFileHeader loadTableHeader(BinaryReader reader) {
		final StructTableFileHeader tblHeader = new StructTableFileHeader(reader);
		final long length = (tblHeader.tableNameLength - 1) * 2;
		tblHeader.name = TextUtil.extractUTF16(reader, length);
		return tblHeader;
	}

	protected StructTableFieldHeader[] loadTableFields(StructTableFileHeader header, long postHeaderPosition, BinaryReader reader) {
		final int fieldHeaderPosition = (int) (postHeaderPosition + header.fieldOffset);

		final StructTableFieldHeader[] fields = new StructTableFieldHeader[(int) header.fieldCount];
		for (int i = 0; i < fields.length; ++i) {
			reader.seek(Seek.BEGIN, fieldHeaderPosition + (i * StructTableFieldHeader.SIZE_IN_BYTES));
			fields[i] = new StructTableFieldHeader(reader);
		}

		// data is 16byte aligned, so for an odd number of fields, the data
		// contains 0-padding to align the following data correctly:
		// 2*16 - 24 = 8
		final int offsetPadding = (fields.length % 2) == 0 ? 0 : 8;
		final int tblNamePosition = fieldHeaderPosition + offsetPadding + (StructTableFieldHeader.SIZE_IN_BYTES * fields.length);

		for (final StructTableFieldHeader field : fields) {
			final StructTableFieldHeader fieldHeader = field;
			reader.seek(Seek.BEGIN, tblNamePosition + fieldHeader.nameOffset);
			fieldHeader.name = TextUtil.extractUTF16(reader, (fieldHeader.nameLength - 1) * 2);
		}

		return fields;
	}

	protected List<TableRecord> loadTableRecords(StructTableFileHeader header, StructTableFieldHeader[] fields, long postHeaderPosition, BinaryReader reader) {

		final long tblRecordPosition = postHeaderPosition + header.recordOffset;
		final List<TableRecord> records = new ArrayList<>((int) header.recordCount);

		for (int i = 0; i < header.recordCount; ++i) {
			reader.seek(Seek.BEGIN, tblRecordPosition + (i * header.recordSize));

			final TableRecord record = new TableRecord(fields.length);
			records.add(record);

			for (int j = 0; j < fields.length; ++j) {
				final StructTableFieldHeader field = fields[j];

				switch (field.getFieldDataType()) {
					case INT32:
						record.data[j] = reader.readInt32();
						break;
					case FLOAT:
						record.data[j] = reader.readFloat32();
						break;
					case BOOL:
						final int val = reader.readInt32();
						record.data[j] = val != 0;
						break;
					case INT64:
						record.data[j] = reader.readInt64();
						break;
					case STRING:
						final long offsetA = reader.readUInt32();
						final long offsetB = reader.readUInt32();
						final long strOffset = Math.max(offsetA, offsetB);
						if (offsetA == 0) {
							reader.readInt32();
							// for some reason the field can have an additional 4
							// bytes,
							// if the first offset is 0
						}

						final long lastPosition = reader.getPosition();
						final long dataOffset = tblRecordPosition + strOffset;

						reader.seek(Seek.BEGIN, dataOffset);
						final String value = TextUtil.extractNullTerminatedUTF16(reader);
						record.data[j] = value;
						reader.seek(Seek.BEGIN, lastPosition);
						break;
					default:
						throw new IllegalArgumentException();
				}
			}

		}

		return records;
	}

	protected int[] loadTableLookup(StructTableFileHeader header, long postHeaderPosition, BinaryReader reader) {
		reader.seek(Seek.BEGIN, postHeaderPosition + header.lookupOffset);
		final int[] lookUps = new int[(int) header.maxId];
		for (int i = 0; i < lookUps.length; ++i) {
			lookUps[i] = reader.readInt32();
		}
		return lookUps;
	}

}
