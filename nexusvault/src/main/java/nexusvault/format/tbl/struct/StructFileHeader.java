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

package nexusvault.format.tbl.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.BIT_64;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.NotUsedForPaddingException;
import nexusvault.shared.exception.StructException;

public final class StructFileHeader implements ReadAndWritable {

	public static final int SIGNATURE = 'D' << 24 | 'T' << 16 | 'B' << 8 | 'L';

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructFileHeader.class) /* 96 */;

	static {
		if (SIZE_IN_BYTES != 0x60) {
			throw new StructException("Invalid struct size");
		}
	}

	/** 'DTBL' */
	@Order(1)
	@StructField(BIT_32)
	public int signature; // 0x000

	@Order(2)
	@StructField(BIT_32)
	public long version; // 0x004

	/** number of UTF-16 encoded characters, aligned to 16 byte and null terminated */
	@Order(3)
	@StructField(UBIT_64) // maybe a int64
	public long nameLength; // 0x008

	/**  */
	@Order(4)
	@StructField(UBIT_64)
	private long padding_010; // 0x010

	/** Size of a single record in bytes */
	@Order(5)
	@StructField(UBIT_64) // maybe a int64
	public long recordSize; // 0x018

	/** Number of fields */
	@Order(6)
	@StructField(UBIT_64)
	public long fieldCount; // 0x020

	/** Start offset for the first field */
	@Order(7)
	@StructField(UBIT_64)
	public long fieldOffset; // 0x028

	/** Number of records */
	@Order(8)
	@StructField(UBIT_64) // maybe a int64
	public long recordCount; // 0x030

	/** Size of all records in bytes */
	@Order(9)
	@StructField(UBIT_64)
	public long totalRecordsSize; // 0x038

	/** Start offset for the first record */
	@Order(10)
	@StructField(UBIT_64)
	public long recordOffset; // 0x040

	/** id to index lookup */
	@Order(11)
	@StructField(UBIT_64)
	public long lookupCount; // 0x048

	@Order(12)
	@StructField(UBIT_64)
	public long lookupOffset; // 0x050

	@Order(13)
	@StructField(BIT_64)
	private long padding_058; // 0x058

	public StructFileHeader() {
		this.signature = SIGNATURE;
	}

	public StructFileHeader(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.signature = reader.readInt32(); // o:4
		this.version = reader.readUInt32(); // o:8
		this.nameLength = reader.readInt64(); // o:12
		this.padding_010 = reader.readInt64(); // o:24
		this.recordSize = reader.readInt64(); // o:28
		this.fieldCount = reader.readInt64(); // o:40
		this.fieldOffset = reader.readInt64(); // o:48
		this.recordCount = reader.readInt64(); // o:52
		this.totalRecordsSize = reader.readInt64(); // o:64
		this.recordOffset = reader.readInt64(); // o:72
		this.lookupCount = reader.readInt64(); // o:80
		this.lookupOffset = reader.readInt64(); // o:88
		this.padding_058 = reader.readInt64(); // o:96

		if (this.padding_010 != 0) {
			throw new NotUsedForPaddingException("padding_010");
		}
		if (this.padding_058 != 0) {
			throw new NotUsedForPaddingException("padding_058");
		}
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.signature);
		writer.writeInt32(this.version);
		writer.writeInt64(this.nameLength);
		writer.writeInt64(this.padding_010);
		writer.writeInt64(this.recordSize);
		writer.writeInt64(this.fieldCount);
		writer.writeInt64(this.fieldOffset);
		writer.writeInt64(this.recordCount);
		writer.writeInt64(this.totalRecordsSize);
		writer.writeInt64(this.recordOffset);
		writer.writeInt64(this.lookupCount);
		writer.writeInt64(this.lookupOffset);
		writer.writeInt64(this.padding_058);
	}

}
