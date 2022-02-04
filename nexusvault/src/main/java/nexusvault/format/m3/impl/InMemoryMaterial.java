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

import java.util.List;

import nexusvault.format.m3.Material;
import nexusvault.format.m3.MaterialDescription;
import nexusvault.format.m3.struct.StructMaterial;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryMaterial implements Material {

	private final int idx;
	private final StructMaterial struct;
	private final InMemoryModel parent;

	public InMemoryMaterial(int idx, StructMaterial struct, InMemoryModel model) {
		this.idx = idx;
		this.struct = struct;
		this.parent = model;
	}

	public int getMaterialIndex() {
		return this.idx;
	}

	@Override
	public int getMaterialDescriptorCount() {
		return this.struct.materialDescription.getArrayLength();
	}

	@Override
	public MaterialDescription getMaterialDescription(int idx) {
		final var struct = this.parent.getStruct(this.struct.materialDescription, idx);
		final var model = new InMemoryMaterialDescription(idx, struct, this);
		return model;
	}

	@Override
	public List<MaterialDescription> getMaterialDescriptions() {
		return this.parent.getAllStructsPacked(this.struct.materialDescription, (idx, struct) -> new InMemoryMaterialDescription(idx, struct, this));
	}

}
