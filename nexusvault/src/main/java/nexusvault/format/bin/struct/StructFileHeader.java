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

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.UBIT_64;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructFileHeader implements ReadAndWritable {

	public static final int SIGNATURE = 'L' << 24 | 'T' << 16 | 'E' << 8 | 'X';

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructFileHeader.class);

	static {
		if (SIZE_IN_BYTES != 0x60) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(BIT_32)
	public int signature; // 0x00

	@Order(2)
	@StructField(BIT_32)
	public int version; // 0x04

	/**
	 * Values are hardcoded
	 * <ul>
	 * <li>1 - english
	 * <li>2 - german
	 * <li>3 - french
	 * <li>4 - korean
	 * </ul>
	 */
	@Order(3)
	@StructField(UBIT_64)
	public long languageType; // 0x08

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(4)
	@StructField(UBIT_64)
	public long languageTagNameLength; // 0x10

	@Order(5)
	@StructField(UBIT_64)
	public long languageTagNameOffset; // 0x18

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(6)
	@StructField(UBIT_64)
	public long languageShortNameLength; // 0x20

	@Order(7)
	@StructField(UBIT_64)
	public long languageShortNameOffset; // 0x28

	/**
	 * UTF16 encoded, 0 terminated
	 */
	@Order(8)
	@StructField(UBIT_64)
	public long languageLongNameLength; // 0x30

	@Order(9)
	@StructField(UBIT_64)
	public long languageLongNameOffset; // 0x38

	@Order(10)
	@StructField(UBIT_64)
	public long entryCount; // 0x40

	@Order(11)
	@StructField(UBIT_64)
	public long entryOffset; // 0x48

	/**
	 * number of characters, each is UTF16 encoded
	 */
	@Order(12)
	@StructField(UBIT_64)
	public long totalTextSize; // 0x50

	@Order(13)
	@StructField(UBIT_64)
	public long textOffset; // 0x58

	public StructFileHeader() {
		this.signature = SIGNATURE;
	}

	public StructFileHeader(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.signature = reader.readInt32();
		this.version = reader.readInt32();
		this.languageType = reader.readInt64();
		this.languageTagNameLength = reader.readInt64();
		this.languageTagNameOffset = reader.readInt64();
		this.languageShortNameLength = reader.readInt64();
		this.languageShortNameOffset = reader.readInt64();
		this.languageLongNameLength = reader.readInt64();
		this.languageLongNameOffset = reader.readInt64();
		this.entryCount = reader.readInt64();
		this.entryOffset = reader.readInt64();
		this.totalTextSize = reader.readInt64();
		this.textOffset = reader.readInt64();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.signature);
		writer.writeInt32(this.version);
		writer.writeInt64(this.languageType);
		writer.writeInt64(this.languageTagNameLength);
		writer.writeInt64(this.languageTagNameOffset);
		writer.writeInt64(this.languageShortNameLength);
		writer.writeInt64(this.languageShortNameOffset);
		writer.writeInt64(this.languageLongNameLength);
		writer.writeInt64(this.languageLongNameOffset);
		writer.writeInt64(this.entryCount);
		writer.writeInt64(this.entryOffset);
		writer.writeInt64(this.totalTextSize);
		writer.writeInt64(this.textOffset);
	}

}
