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
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructIndexFile implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructIndexFile.class);

	static {
		if (SIZE_IN_BYTES != 0x38) {
			throw new StructException();
		}
	}

	/**
	 * A {@code nameOffset} of <code>-1</code> indicates, that no offset is set
	 */
	@Order(1)
	@StructField(DataType.UBIT_32)
	public int nameOffset; // 0x000

	@Order(2)
	@StructField(DataType.BIT_32)
	public int flags; // 0x004

	@Order(3)
	@StructField(DataType.UBIT_64)
	public long writeTime; // 0x008

	@Order(4)
	@StructField(DataType.UBIT_64)
	public long uncompressedSize; // 0x010

	@Order(5)
	@StructField(DataType.UBIT_64)
	public long compressedSize; // 0x018

	@Order(6)
	@StructField(value = DataType.BIT_8, length = 20)
	public byte[] hash; // 0x020

	/**
	 * Unknown field. Sometimes used, sometimes not.
	 */
	@Order(7)
	@StructField(DataType.BIT_32)
	public int unk_034; // 0x034

	public StructIndexFile() {
		this.hash = new byte[20];
	}

	public StructIndexFile(BinaryReader reader) {
		this();
		read(reader);
	}

	public StructIndexFile(int nameOffset, int flags, long writeTime, long uncompressedSize, long compressedSize, byte[] hash, int unk_034) {
		this.nameOffset = nameOffset;
		this.flags = flags;
		this.writeTime = writeTime;
		this.uncompressedSize = uncompressedSize;
		this.compressedSize = compressedSize;
		this.hash = hash;
		this.unk_034 = unk_034;
	}

	@Override
	public void read(BinaryReader reader) {
		this.nameOffset = (int) reader.readUInt32();
		this.flags = (int) reader.readUInt32();
		this.writeTime = reader.readInt64();
		this.uncompressedSize = reader.readInt64();
		this.compressedSize = reader.readInt64();
		reader.readInt8(this.hash, 0, this.hash.length);
		this.unk_034 = reader.readInt32();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.nameOffset);
		writer.writeInt32(this.flags);
		writer.writeInt64(this.writeTime);
		writer.writeInt64(this.uncompressedSize);
		writer.writeInt64(this.compressedSize);
		writer.writeInt8(this.hash, 0, this.hash.length);
		writer.writeInt32(this.unk_034);
	}

}