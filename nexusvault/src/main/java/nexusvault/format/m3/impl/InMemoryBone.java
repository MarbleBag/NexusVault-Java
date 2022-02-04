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

import nexusvault.format.m3.Bone;
import nexusvault.format.m3.struct.StructBones;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryBone implements Bone {

	private final int idx;
	private final StructBones struct;
	private final InMemoryModel model;

	public InMemoryBone(int idx, StructBones struct, InMemoryModel model) {
		this.idx = idx;
		this.struct = struct;
		this.model = model;
	}

	@Override
	public float[] getTransformationMatrix() {
		return this.struct.matrix_0D0;
	}

	@Override
	public float[] getInverseTransformationMatrix() {
		return this.struct.matrix_110;
	}

	@Override
	public int getBoneIndex() {
		return this.idx;
	}

	@Override
	public float getLocationX() {
		return this.struct.x;
	}

	@Override
	public float getLocationY() {
		return this.struct.y;
	}

	@Override
	public float getLocationZ() {
		return this.struct.z;
	}

	@Override
	public boolean hasParentBone() {
		return this.struct.parentId != -1;
	}

	@Override
	public int getParentBoneReference() {
		return this.struct.parentId;
	}

}
