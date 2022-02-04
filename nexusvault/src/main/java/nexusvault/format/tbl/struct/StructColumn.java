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

import static kreed.reflection.struct.DataType.UBIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructColumn implements ReadAndWritable {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructColumn.class) /* 24 */;

	static {
		if (SIZE_IN_BYTES != 0x18) {
			throw new StructException();
		}
	}

	/** number of UTF-16 encoded characters, aligned to 16 byte and null terminated */
	@Order(1)
	@StructField(UBIT_64)
	public long nameLength; // 0x00

	@Order(3)
	@StructField(UBIT_64)
	public long nameOffset; // 0x08

	@Order(4)
	@StructField(UBIT_32)
	public int type; // 0x10

	/**
	 * <ul>
	 * <li>int32: 24 or (very rare) 16 or 112
	 * <li>int64: 24
	 * <li>string: 104 or (very rare) 8 or 96
	 * <li>float: 24 or (very rare) 16
	 * <li>bool: 24
	 * </ul>
	 */
	@Order(6)
	@StructField(UBIT_32)
	public long unk2; // 0x14

	public StructColumn() {

	}

	public StructColumn(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.nameLength = reader.readInt64(); // o:4
		this.nameOffset = reader.readInt64(); // o:16
		this.type = reader.readInt32(); // o:18
		this.unk2 = reader.readUInt32(); // o:24
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt64(this.nameLength);
		writer.writeInt64(this.nameOffset);
		writer.writeInt32(this.type);
		writer.writeInt32(this.unk2);
	}

}
