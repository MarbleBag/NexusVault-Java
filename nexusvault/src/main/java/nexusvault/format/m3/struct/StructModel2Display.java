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
import nexusvault.shared.exception.StructException;

public final class StructModel2Display implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructModel2Display.class);

	static {
		if (SIZE_IN_BYTES != 0x04) {
			throw new StructException("Invalid struct size");
		}
	}

	/**
	 * Is referenced by the Creature2Display.tbl in column modelMeshId00 - modelMeshId15.
	 * <p>
	 * The index of this struct references {@link StructMesh#meshGroupId}
	 */
	@Order(1)
	@StructField(value = DataType.UBIT_16)
	public int modelMeshId; // 0x000

	/**
	 * Default to display. In case no further information about which mesh groups to render is given, render each mesh groups with a value of 1
	 * <ul>
	 * <li>0 - not default
	 * <li>1 - default
	 * </ul>
	 * Other values were not seen
	 */
	@Order(2)
	@StructField(value = DataType.UBIT_16)
	public int default2Render; // 0x002

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
	}

}
