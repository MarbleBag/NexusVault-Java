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

package nexusvault.format.m3.struct;

import static kreed.reflection.struct.DataType.STRUCT;
import static kreed.reflection.struct.DataType.UBIT_32;

import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.format.m3.pointer.DATP_S4_S12;
import nexusvault.format.m3.pointer.DATP_S4_S6;
import nexusvault.format.m3.pointer.DATP_S4_S8;
import nexusvault.shared.exception.StructException;

public final class StructUnk152 implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructUnk152.class);

	static {
		if (SIZE_IN_BYTES != 0x98) {
			throw new StructException("Invalid struct size");
		}
	}

	@Order(1)
	@StructField(UBIT_32)
	public long unk_value_000; // 0x000

	@Order(2)
	@StructField(STRUCT)
	public DATP_S4_S6 unk_offset_008;// 0x008

	@Order(3)
	@StructField(STRUCT)
	public DATP_S4_S6 unk_offset_020; // 0x020

	@Order(4)
	@StructField(STRUCT)
	public DATP_S4_S8 unk_offset_038; // 0x038

	@Order(5)
	@StructField(STRUCT)
	public DATP_S4_S8 unk_offset_050; // 0x050

	@Order(6)
	@StructField(STRUCT)
	public DATP_S4_S12 unk_offset_068; // 0x068

	@Order(7)
	@StructField(STRUCT)
	public DATP_S4_S12 unk_offset_080; // 0x080

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, this.unk_offset_008);
		process.process(fileReader, dataPosition, this.unk_offset_020);
		process.process(fileReader, dataPosition, this.unk_offset_038);
		process.process(fileReader, dataPosition, this.unk_offset_050);
		process.process(fileReader, dataPosition, this.unk_offset_068);
		process.process(fileReader, dataPosition, this.unk_offset_080);
	}

}
