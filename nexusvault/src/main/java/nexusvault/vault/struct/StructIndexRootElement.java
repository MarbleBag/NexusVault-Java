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

package nexusvault.vault.struct;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.DataType;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructIndexRootElement implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIndexRootElement.class);
	/** used for index files */
	public static final int SIGNATURE_AIDX = 'A' << 24 | 'I' << 16 | 'D' << 8 | 'X';

	static {
		if (SIZE_IN_BYTES != 0x10) {
			throw new StructException("Invalid struct size");
		}
	}

	@StructField(DataType.BIT_32)
	public int signature;

	@StructField(DataType.BIT_32)
	public int version;

	@StructField(DataType.BIT_32)
	public int buildNumber;

	@StructField(DataType.BIT_32)
	public int headerIdx;

	public StructIndexRootElement() {

	}

	public StructIndexRootElement(int signature, int version, int buildNumber, int headerIdx) {
		this.signature = signature;
		this.version = version;
		this.buildNumber = buildNumber;
		this.headerIdx = headerIdx;
	}

	public StructIndexRootElement(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.signature = reader.readInt32();
		this.version = reader.readInt32();
		this.buildNumber = reader.readInt32();
		this.headerIdx = reader.readInt32();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.signature);
		writer.writeInt32(this.version);
		writer.writeInt32(this.buildNumber);
		writer.writeInt32(this.headerIdx);
	}
}
