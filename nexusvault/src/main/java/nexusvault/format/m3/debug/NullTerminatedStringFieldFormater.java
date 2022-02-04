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

package nexusvault.format.m3.debug;

import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.format.m3.debug.Table.TableCell;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.pointer.ArrayTypePointer;
import nexusvault.shared.Text;

public final class NullTerminatedStringFieldFormater implements FieldFormater {
	@Override
	public void processField(DebugInfo debugger, TableCell cell, FieldReader fieldReader) {
		for (int i = 0; i < fieldReader.size(); ++i) {
			final Object val = fieldReader.get();
			final ArrayTypePointer<?> ptr = (ArrayTypePointer) val;
			if (ptr.hasType()) {
				cell.addEntry(String.format("Error: String formater is not for typed pointer"));
			} else {
				if (ptr.getOffset() == 0) {
					continue;
				}

				final BytePositionTracker data = debugger.getDataModel();
				data.setPosition(ptr.getOffset());

				final String txt = Text.readNullTerminatedUTF16(new ByteBufferBinaryReader(data.getData()));
				cell.addEntry(txt);
			}
		}
	}
}