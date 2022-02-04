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

public final class StructFormatTask implements Task {

	private final long dataOffset;
	private final Class<?> structClass;
	private final int structCount;
	private final TaskOutput<? super Table> out;

	public StructFormatTask(long dataOffset, Class<?> structClass, int structCount, TaskOutput<? super Table> out) {
		this.dataOffset = dataOffset;
		this.structClass = structClass;
		this.structCount = structCount;
		this.out = out;
	}

	@Override
	public void runTask(DebugInfo debugger) {
		final StructFormater formater = debugger.getStructFormater(this.structClass);
		final Table table = formater.formatTable(debugger, this.dataOffset, this.structClass, this.structCount);
		this.out.setOutput(table);
	}

}