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

package nexusvault.export.m3.gltf.internal;

public enum GlTFComponentType {
	INT8(5120, 1),
	UINT8(5121, 1),
	INT16(5122, 2),
	UINT16(5123, 2),
	UINT32(5125, 4),
	FLOAT(5126, 4);

	private final int id;
	private final int byteCount;

	private GlTFComponentType(int id, int byteCount) {
		this.id = id;
		this.byteCount = byteCount;
	}

	public int getId() {
		return this.id;
	}

	public int getByteCount() {
		return this.byteCount;
	}
}