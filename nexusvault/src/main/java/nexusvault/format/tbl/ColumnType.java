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

package nexusvault.format.tbl;

import nexusvault.format.tbl.struct.StructColumn;

public enum ColumnType {
	UNK(0),
	FLOAT(4),
	INT32(3),
	BOOL(11),
	INT64(20),
	STRING(130);

	public final int value;

	private ColumnType(int flag) {
		this.value = flag;
	}

	public static ColumnType resolve(StructColumn column) {
		return resolve(column.type);
	}

	public static ColumnType resolve(int type) {
		for (final ColumnType t : ColumnType.values()) {
			if (t.value == type) {
				return t;
			}
		}
		return UNK;
	}
}