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

package nexusvault.export.m3.gltf.internal.vertex;

import kreed.io.util.BinaryWriter;
import nexusvault.export.m3.gltf.internal.GlTFComponentType;
import nexusvault.export.m3.gltf.internal.GlTFMeshAttribute;
import nexusvault.export.m3.gltf.internal.GlTFType;
import nexusvault.format.m3.Vertex;

public final class VFWBoneWeights extends VertexFieldWriter {

	private int[] min;
	private int[] max;

	public VFWBoneWeights(int offsetWithinVertex) {
		super("BoneWeight", GlTFComponentType.FLOAT, GlTFType.VEC4, GlTFMeshAttribute.WEIGHTS, offsetWithinVertex);
		resetField();
	}

	@Override
	public void writeTo(BinaryWriter writer, Vertex vertex) {
		final int a = vertex.getBoneWeight1();
		final int b = vertex.getBoneWeight2();
		final int c = vertex.getBoneWeight3();
		final int d = vertex.getBoneWeight4();
		writer.writeFloat32(a / 255f);
		writer.writeFloat32(b / 255f);
		writer.writeFloat32(c / 255f);
		writer.writeFloat32(d / 255f);

		this.min[0] = Math.min(this.min[0], a & 0xFF);
		this.min[1] = Math.min(this.min[1], b & 0xFF);
		this.min[2] = Math.min(this.min[2], c & 0xFF);
		this.min[3] = Math.min(this.min[3], d & 0xFF);
		this.max[0] = Math.max(this.max[0], a & 0xFF);
		this.max[1] = Math.max(this.max[1], b & 0xFF);
		this.max[2] = Math.max(this.max[2], c & 0xFF);
		this.max[3] = Math.max(this.max[3], d & 0xFF);
	}

	@Override
	public void resetField() {
		super.resetField();

		this.min = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
		this.max = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };

		// min = new int[] { 0, 0, 0, 0 };
		// max = new int[] { 255, 255, 255, 255 };
	}

	@Override
	public boolean hasMinimum() {
		return false;
	}

	@Override
	public boolean hasMaximum() {
		return false;
	}

	@Override
	public Number[] getMinimum() {
		return new Number[] { this.min[0], this.min[1], this.min[2], this.min[3] };
	}

	@Override
	public Number[] getMaximum() {
		return new Number[] { this.max[0], this.max[1], this.max[2], this.max[3] };
	}
}