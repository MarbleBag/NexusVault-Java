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

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.format.m3.pointer.DATP_S4_S12;
import nexusvault.format.m3.pointer.DATP_S4_S4;
import nexusvault.shared.exception.StructException;

/**
 * Contains:
 * <ul>
 * <li>{@link StructTexture texture} selectors, linked under {@link StructM3Header header}
 * </ul>
 * <p>
 * TODO
 */
public final class StructMaterialDescriptor implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructMaterialDescriptor.class);

	static {
		if (SIZE_IN_BYTES != 0x128) {
			throw new StructException("Invalid struct size");
		}
	}

	/**
	 * points to the texture containing the diffuse, etc
	 * <p>
	 * The value <code>65535</code> indicates that no texture is set, it is also possible that this value is actually signed and <code>-1</code> is used to
	 * indicate that the value is not set
	 */
	@Order(1)
	@StructField(DataType.UBIT_16)
	public int textureSelectorA; // 000

	/** points to the texture containing the normals, etc */
	@Order(2)
	@StructField(DataType.UBIT_16)
	public int textureSelectorB; // 002

	@Order(3)
	@StructField(value = DataType.BIT_8, length = 20)
	public int[] gap_004; // 004

	@Order(4)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_018;

	@Order(5)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_030;

	@Order(6)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_048;

	@Order(7)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_060;

	@Order(8)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_078;

	@Order(9)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_090;

	@Order(10)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_0A8;

	@Order(11)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_0C0;

	@Order(12)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_0D8;

	@Order(13)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_0F0;

	@Order(14)
	@StructField(DataType.STRUCT)
	public DATP_S4_S12 unk_offset_108;

	@Order(15)
	@StructField(value = DataType.BIT_8, length = 8)
	public int[] gap_120;

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, this.unk_offset_018);
		process.process(fileReader, dataPosition, this.unk_offset_030);
		process.process(fileReader, dataPosition, this.unk_offset_048);
		process.process(fileReader, dataPosition, this.unk_offset_060);
		process.process(fileReader, dataPosition, this.unk_offset_078);
		process.process(fileReader, dataPosition, this.unk_offset_090);
		process.process(fileReader, dataPosition, this.unk_offset_0A8);
		process.process(fileReader, dataPosition, this.unk_offset_0C0);
		process.process(fileReader, dataPosition, this.unk_offset_0D8);
		process.process(fileReader, dataPosition, this.unk_offset_0F0);
		process.process(fileReader, dataPosition, this.unk_offset_108);
	}
}
