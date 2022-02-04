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

import static kreed.reflection.struct.DataType.BIT_16;
import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.BIT_8;
import static kreed.reflection.struct.DataType.STRUCT;
import static kreed.reflection.struct.DataType.UBIT_16;

import kreed.reflection.struct.DataType;
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

public final class StructBones implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructBones.class);

	static {
		if (SIZE_IN_BYTES != 0x160) {
			throw new StructException();
		}
	}

	private static class StructInnerBones {
		public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructInnerBones.class);

		@Order(1)
		@StructField(value = BIT_8, length = 12)
		public byte[] gap_000; // 0x0D0

		/**
		 * Close to always 0, for 9 models this is not 0
		 */
		@Order(2)
		@StructField(value = BIT_8, length = 4)
		public byte[] gap_00C; // 0x0DC
	}

	// Most of the time this field is 65535 (-1)
	/**
	 * This number seems to be equal between a character bone and an item bone if both are 'at the same place', maybe this number acts as a kind of bind value
	 * for items or it binds a bone to something else (for example: this is a finger bone)
	 * <p>
	 * Except -1, contains no number twice. <br>
	 * In some cases this number becomes bigger than max(BIT_16), so it is probably not 1, but 2 numbers, each 16_BIT
	 */
	@Order(1)
	@StructField(BIT_16)
	public int gap_000; // 0x000

	@Order(2)
	@StructField(UBIT_16)
	public int gap_002; // 0x002

	/**
	 * A value of <code>-1</code> indicates this bone has no parent
	 */
	@Order(3)
	@StructField(BIT_16)
	public int parentId; // 0x004

	/**
	 * Seems to be a grouping index to group bones which 'belong together'
	 */
	@Order(4)
	@StructField(value = BIT_8, length = 2)
	public byte[] gap_006; // 0x006

	/**
	 * Are the {@link #gap_000} values of two bones from two m3 equal, so are their {@link #gap_008} values.
	 * <p>
	 * In case of two bones with {@link #gap_000} = -1, {@link #gap_008} can still be equal to each other <br>
	 * Good chance that {@link #gap_008} controls which bones 'go together', but it's not clear how its value is generated
	 * <p>
	 * This could be an enum. It seems that bones with the same value are roughly at the 'same place' of a model. Every bone of the the left feet has the same
	 * number, etc.
	 */
	@Order(5)
	@StructField(value = DataType.UBIT_32)
	public long gap_008; // 0x008

	@Order(6)
	@StructField(BIT_32)
	public int padding_00C; // 0x00C

	// unk_block_010[2] is related to parent.unk_block_010[0]. Both pointer have (mostly) always the same amount of elements
	// Those values seem to be 0, for leaf bones
	@Order(7)
	@StructField(value = STRUCT, length = 4)
	public DATP_S4_S6[] unk_block_010;

	@Order(8)
	@StructField(value = STRUCT, length = 2)
	public DATP_S4_S8[] bone_animation; // 0x070

	@Order(9)
	@StructField(value = STRUCT, length = 2)
	public DATP_S4_S12[] unk_block_0A0; // 0x0A0

	/**
	 * Looks like a column-major transformation matrix
	 */
	@Order(10)
	@StructField(value = BIT_32, length = 16)
	public float[] matrix_0D0; // 0x0D0

	/**
	 * Looks like the inverse column-major transformation matrix
	 */
	@Order(11)
	@StructField(value = BIT_32, length = 16)
	public float[] matrix_110; // 0x110

	@Order(12)
	@StructField(BIT_32)
	public float x; // 0x150

	@Order(13)
	@StructField(BIT_32)
	public float y; // 0x154

	@Order(14)
	@StructField(BIT_32)
	public float z; // 0x158

	@Order(15)
	@StructField(BIT_32)
	public int padding_15C; // 0x15C

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		for (final DATP_S4_S6 p : this.unk_block_010) {
			process.process(fileReader, dataPosition, p);
		}

		for (final DATP_S4_S8 p : this.bone_animation) {
			process.process(fileReader, dataPosition, p);
		}

		for (final DATP_S4_S12 p : this.unk_block_0A0) {
			process.process(fileReader, dataPosition, p);
		}

		if (this.padding_15C != 0) {
			throw new IllegalStateException("padding_15C contains value");
		}
	}

}
