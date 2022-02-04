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

import java.util.ArrayList;
import java.util.List;

import nexusvault.format.m3.debug.Table.DataType;
import nexusvault.format.m3.debug.Table.TableCell;
import nexusvault.format.m3.debug.Table.TableColumn;
import nexusvault.format.m3.debug.Table.TableRow;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.pointer.ArrayTypePointer;

final class ATPFieldFormater implements FieldFormater {

	private static final class SingleDataArrayFormatTask implements Task {

		private final long offset;
		private final int sizeOfElement;
		private final int numberOfElements;
		private final TaskOutput<? super Table> out;

		public SingleDataArrayFormatTask(long offset, int sizeOfElement, int numberOfElements, TaskOutput<? super Table> out) {
			this.offset = offset;
			this.sizeOfElement = sizeOfElement;
			this.numberOfElements = numberOfElements;
			this.out = out;
		}

		@Override
		public void runTask(DebugInfo debugger) {
			final Table table = createInitialTable();
			final BytePositionTracker data = debugger.getDataModel();
			data.setPosition(this.offset);

			final byte[][] loadedData = new byte[this.numberOfElements][this.sizeOfElement];

			for (final byte[] line : loadedData) {
				data.getData().get(line);
			}

			for (final byte[] line : loadedData) {
				final TableRow nextRow = table.addNewRow();
				for (int c = 0; c < this.sizeOfElement; ++c) {
					final TableColumn column = table.getColumn(c);
					final TableCell cell = column.getCell(nextRow);
					cell.addEntry(line[c] & 0xFF);
				}
			}
			this.out.setOutput(table);
		}

		private Table createInitialTable() {
			final List<TableColumn> columns = new ArrayList<>(this.sizeOfElement);
			for (int i = 0; i < this.sizeOfElement; ++i) {
				columns.add(new TableColumn("Column " + i, "" + i, DataType.UBYTE));
			}
			return new Table(columns);
		}
	}

	@Override
	public void processField(DebugInfo debugger, TableCell cell, FieldReader fieldReader) {
		for (int i = 0; i < fieldReader.size(); ++i) {
			final Object val = fieldReader.get();
			final ArrayTypePointer<?> ptr = (ArrayTypePointer) val;
			if (ptr.hasType()) {
				final TaskOutput<Table> callback = (result) -> cell.addEntry(result);
				debugger.queueTask(new StructFormatTask(ptr.getOffset(), ptr.getTypeOfElement(), ptr.getArrayLength(), callback));
			} else {
				final TaskOutput<Table> callback = (result) -> cell.addEntry(result);
				debugger.queueTask(new SingleDataArrayFormatTask(ptr.getOffset(), ptr.getSizeOfElement(), ptr.getArrayLength(), callback));
				// cell.addEntry(String.format("ATP [%d->%d[%d]]", ptr.getArraySize(), ptr.getOffset(), ptr.getElementSize()));
			}
		}
	}
}