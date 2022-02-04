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

package nexusvault.format.m3.impl;

import nexusvault.format.m3.MaterialDescription;
import nexusvault.format.m3.struct.StructMaterialDescriptor;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryMaterialDescription implements MaterialDescription {

	private final int idx;
	private final StructMaterialDescriptor struct;
	private final InMemoryMaterial model;

	public InMemoryMaterialDescription(int idx, StructMaterialDescriptor struct, InMemoryMaterial model) {
		this.idx = idx;
		this.struct = struct;
		this.model = model;
	}

	@Override
	public int getTextureReferenceA() {
		return this.struct.textureSelectorA;
	}

	@Override
	public int getTextureReferenceB() {
		return this.struct.textureSelectorB;
	}

}
