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

public enum GlTFType {
	SCALAR("SCALAR", 1),
	VEC2("VEC2", 2),
	VEC3("VEC3", 3),
	VEC4("VEC4", 4),
	MAT2("MAT2", 4),
	MAT3("MAT3", 9),
	MAT4("MAT4", 16);

	private final String id;
	private final int componentCount;

	private GlTFType(String id, int componentCount) {
		this.id = id;
		this.componentCount = componentCount;
	}

	public String getId() {
		return this.id;
	}

	public int getComponentCount() {
		return this.componentCount;
	}
}