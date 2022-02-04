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
import nexusvault.format.m3.pointer.ATP_MaterialDescriptor;
import nexusvault.shared.exception.StructException;

/**
 * Contains:
 * <ul>
 * <li>{@link StructMaterialDescriptor material descriptors}
 * </ul>
 * <p>
 * TODO
 */
public final class StructMaterial implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructMaterial.class);

	static {
		if (SIZE_IN_BYTES != 0x30) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(value = DataType.BIT_8, length = 32)
	public int[] gap_000;

	@Order(2)
	@StructField(DataType.STRUCT)
	public ATP_MaterialDescriptor materialDescription;

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, this.materialDescription);
	}
}
