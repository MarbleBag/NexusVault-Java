package nexusvault.format.tbl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.tbl.struct.StructColumnData;
import nexusvault.format.tbl.struct.StructTableFileHeader;

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
		final StructColumnData[] tableFields = loadTableFields(tableHeader, postHeaderPosition, reader);

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
		final long length = (tblHeader.nameLength - 1) * 2;
		// tblHeader.name = TextUtil.extractUTF16(reader, length);
		return tblHeader;
	}

	protected StructColumnData[] loadTableFields(StructTableFileHeader header, long postHeaderPosition, BinaryReader reader) {
		final int fieldHeaderPosition = (int) (postHeaderPosition + header.fieldOffset);

		final StructColumnData[] fields = new StructColumnData[(int) header.fieldCount];
		for (int i = 0; i < fields.length; ++i) {
			reader.seek(Seek.BEGIN, fieldHeaderPosition + i * StructColumnData.SIZE_IN_BYTES);
			fields[i] = new StructColumnData(reader);
		}

		// data is 16byte aligned, so for an odd number of fields, the data
		// contains 0-padding to align the following data correctly:
		// 2*16 - 24 = 8
		final int offsetPadding = fields.length % 2 == 0 ? 0 : 8;
		final int tblNamePosition = fieldHeaderPosition + offsetPadding + StructColumnData.SIZE_IN_BYTES * fields.length;

		for (final StructColumnData field : fields) {
			final StructColumnData fieldHeader = field;
			reader.seek(Seek.BEGIN, tblNamePosition + fieldHeader.nameOffset);
			// fieldHeader.name = TextUtil.extractUTF16(reader, (fieldHeader.nameLength - 1) * 2);
		}

		return fields;
	}

	protected List<TableRecord> loadTableRecords(StructTableFileHeader header, StructColumnData[] fields, long postHeaderPosition, BinaryReader reader) {

		final long tblRecordPosition = postHeaderPosition + header.recordOffset;
		final List<TableRecord> records = new ArrayList<>((int) header.recordCount);

		for (int i = 0; i < header.recordCount; ++i) {
			reader.seek(Seek.BEGIN, tblRecordPosition + i * header.recordSize);

			final TableRecord record = new TableRecord(fields.length);
			records.add(record);

			for (int j = 0; j < fields.length; ++j) {
				final StructColumnData field = fields[j];

				switch (field.getDataType()) {
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
						if (reader.getPosition() % 8 != 0) {
							reader.readInt32(); // padding, all strings are 8 byte aligned.
						}

						final long offset = reader.readInt64();

						final long lastPosition = reader.getPosition();
						final long dataOffset = tblRecordPosition + offset;

						reader.seek(Seek.BEGIN, dataOffset);
						record.data[j] = TextUtil.extractNullTerminatedUTF16(reader);
						reader.seek(Seek.BEGIN, lastPosition);
						break;
					default:
						System.out.println(String.format("Unknow data type: '%s'", field.dataType));
						throw new IllegalArgumentException();
				}
			}
		}

		return records;
	}

	protected int[] loadTableLookup(StructTableFileHeader header, long postHeaderPosition, BinaryReader reader) {
		reader.seek(Seek.BEGIN, postHeaderPosition + header.lookupOffset);
		final int[] lookUps = new int[(int) header.lookupCount];
		for (int i = 0; i < lookUps.length; ++i) {
			lookUps[i] = reader.readInt32();
		}
		return lookUps;
	}

}
