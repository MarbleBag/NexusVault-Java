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

public enum GlTFMeshAttribute {
	POSITION("POSITION", 1),
	TEXCOORD("TEXCOORD", 2),
	JOINTS("JOINTS_0", 1),
	WEIGHTS("WEIGHTS_0", 1);

	private final String id;
	private final int maxAttributeCount;

	private GlTFMeshAttribute(String id, int maxAttributeCount) {
		this.id = id;
		this.maxAttributeCount = maxAttributeCount;
	}

	public String getAttributeKey() {
		return this.id;
	}
}