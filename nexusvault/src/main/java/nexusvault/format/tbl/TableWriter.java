/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.tbl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.HashSet;

import kreed.io.util.BinaryWriter;
import kreed.io.util.ByteAlignmentUtil;
import kreed.io.util.ByteArrayBinaryWriter;
import kreed.io.util.Seek;
import kreed.io.util.SeekableByteChannelBinaryWriter;
import nexusvault.format.tbl.struct.StructColumn;
import nexusvault.format.tbl.struct.StructFileHeader;
import nexusvault.shared.Text;

public final class TableWriter {
	private TableWriter() {
	}

	public static byte[] toBinary(Table table) {
		final var size = computeSize(table);
		final var buffer = new byte[(int) size];
		write(table, new ByteArrayBinaryWriter(buffer, ByteOrder.LITTLE_ENDIAN));
		return buffer;
	}

	public static long computeSize(Table table) {
		long size = StructFileHeader.SIZE_IN_BYTES;
		size += table.name.length() * 2 + 2;
		size = ByteAlignmentUtil.alignTo16Byte(size);
		size += StructColumn.SIZE_IN_BYTES * table.columns.length;
		size = ByteAlignmentUtil.alignTo16Byte(size);
		for (final var column : table.columns) {
			size = ByteAlignmentUtil.alignTo16Byte(size + column.name.length() * 2 + 2);
		}
		size += calculateRecordSize(table) * table.entries.length;

		final var stringCache = new HashSet<String>();
		for (final var record : table.entries) {
			for (int i = 0; i < table.columns.length; i++) {
				final var column = table.columns[i];
				switch (column.type) {
					case STRING:
						final var value = record[i].toString();
						if (stringCache.add(value)) {
							size += value.length() * 2 + 2;
						}
						break;
					default:
						// ignore
						break;
				}
			}
		}

		size = ByteAlignmentUtil.alignTo16Byte(size);
		size += table.lookup.length * 4;
		return ByteAlignmentUtil.alignTo16Byte(size);
	}

	public static void write(Table table, SeekableByteChannel out) {
		write(table, new SeekableByteChannelBinaryWriter(out, ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN)));
	}

	public static void write(Table table, BinaryWriter writer) {
		final long writeStartPosition = writer.position();
		writer.seek(Seek.CURRENT, StructFileHeader.SIZE_IN_BYTES);
		final long writePostHeaderPosition = writer.position();

		final var header = new StructFileHeader();
		header.signature = StructFileHeader.SIGNATURE;
		header.version = 0;
		header.nameLength = table.name.length() + 1;

		Text.writeNullTerminatedUTF16(writer, table.name);

		{
			ByteAlignmentUtil.alignTo16Byte(writer);
			header.fieldCount = table.columns.length;
			header.fieldOffset = writer.position() - writePostHeaderPosition;

			long nameOffset = 0;
			final var columnData = new StructColumn();
			for (final var column : table.columns) {
				columnData.nameLength = column.name.length() + 1; // null terminated
				columnData.nameOffset = nameOffset;
				columnData.type = column.type.value;
				columnData.unk2 = column.unk2;
				columnData.write(writer);
				nameOffset += ByteAlignmentUtil.alignTo16Byte(columnData.nameLength * 2);
			}

			// ByteAlignmentUtil.alignTo16Byte(writer);
			if (table.columns.length % 2 != 0) {
				writer.seek(Seek.CURRENT, 8);
			}

			for (final var column : table.columns) {
				Text.writeNullTerminatedUTF16(writer, column.name);
				ByteAlignmentUtil.alignTo16Byte(writer);
			}
		}

		{
			ByteAlignmentUtil.alignTo16Byte(writer);
			header.recordSize = calculateRecordSize(table);
			header.recordCount = table.entries.length;
			header.recordOffset = writer.position() - writePostHeaderPosition;

			final var writeRecordPosition = writer.position();
			var strOffset = header.recordCount * header.recordSize;

			final var stringCache = new HashMap<String, Integer>();

			for (var i = 0; i < header.recordCount; ++i) {
				final var record = table.entries[i];
				for (var j = 0; j < header.fieldCount; ++j) {
					switch (table.columns[j].type) {
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
								final var position = writer.position();
								writer.seek(Seek.CURRENT, writeRecordPosition + strOffset - position);
								strOffset += Text.writeNullTerminatedUTF16(writer, value);
								writer.seek(Seek.CURRENT, position - writer.position());
							}
							break;
						default:
							throw new TableException(String.format("Unknow data type: '%s'", table.columns[j].type));
					}
				}
				ByteAlignmentUtil.alignTo8Byte(writer);
			}

			// number of records * size + all strings
			header.totalRecordsSize = strOffset; // strOffset points to the end of the last written string, so this works.
			writer.seek(Seek.CURRENT, writeRecordPosition + strOffset - writer.position());
		}

		{
			ByteAlignmentUtil.alignTo16Byte(writer);
			header.lookupCount = table.lookup.length;
			header.lookupOffset = writer.position() - writePostHeaderPosition;
			final var byteBuffer = ByteBuffer.allocate(table.lookup.length * 4).order(writer.byteOrder());
			byteBuffer.asIntBuffer().put(table.lookup).flip();
			writer.write(byteBuffer);
		}

		{
			final var writeEndOfFile = ByteAlignmentUtil.alignTo16Byte(writer.position());
			final var padding = writeEndOfFile - writer.position();
			for (var i = 0; i < padding; ++i) {
				writer.writeInt8(0);
			}
		}

		writer.seek(Seek.BEGIN, writeStartPosition);
		header.write(writer);
	}

	private static long calculateRecordSize(Table table) {
		long size = 0;
		for (final var column : table.columns) {
			switch (column.type) {
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
					throw new TableException(String.format("Unknow data type: '%s'", column.type));
			}
		}
		return ByteAlignmentUtil.alignTo8Byte(size);
	}

}
