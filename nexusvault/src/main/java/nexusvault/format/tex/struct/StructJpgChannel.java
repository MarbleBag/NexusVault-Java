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

package nexusvault.format.tex.struct;

import static kreed.reflection.struct.DataType.BIT_8;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructJpgChannel implements ReadAndWritable {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructJpgChannel.class); // 0x3;

	static {
		if (SIZE_IN_BYTES != 0x3) {
			throw new StructException();
		}
	}

	/** uint8, 0-100 */
	@Order(1)
	@StructField(BIT_8)
	public byte quality;

	/** uint8, 0-1 */
	@Order(2)
	@StructField(BIT_8)
	public boolean hasColor;

	/** uint8, 0-255 */
	@Order(3)
	@StructField(BIT_8)
	public byte color;

	public StructJpgChannel() {

	}

	public StructJpgChannel(byte quality, boolean hasColor, byte color) {
		this.quality = quality;
		this.hasColor = hasColor;
		this.color = color;
	}

	public StructJpgChannel(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.quality = reader.readInt8();
		this.hasColor = reader.readInt8() != 0;
		this.color = reader.readInt8();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt8(this.quality);
		writer.writeInt8(this.hasColor ? 1 : 0);
		writer.writeInt8(this.color);
	}

}