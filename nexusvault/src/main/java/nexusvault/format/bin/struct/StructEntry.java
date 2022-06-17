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

package nexusvault.format.bin.struct;

import static kreed.reflection.struct.DataType.UBIT_32;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructEntry implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructEntry.class);

	static {
		if (SIZE_IN_BYTES != 0x08) {
			throw new StructException("Invalid struct size");
		}
	}

	@Order(1)
	@StructField(UBIT_32)
	public long id; // 0x0

	/**
	 * Offset starts at {@link StructFileHeader#textOffset}. <br>
	 * Each character is UTF16 encoded, hence, to compute the correct start position, this value needs to be multiplied with 2.
	 */
	@Order(2)
	@StructField(UBIT_32)
	public long characterOffset; // 0x4

	public StructEntry() {

	}

	public StructEntry(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.id = reader.readUInt32();
		this.characterOffset = reader.readUInt32();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.id);
		writer.writeInt32(this.characterOffset);
	}

}
