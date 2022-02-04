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

public final class VFWBoneIndices extends VertexFieldWriter {

	private int[] min;
	private int[] max;
	private final int[] boneLookUp;

	public VFWBoneIndices(int offsetWithinVertex, int[] boneLookUp) {
		super("BoneIdx", GlTFComponentType.UINT16, GlTFType.VEC4, GlTFMeshAttribute.JOINTS, offsetWithinVertex);
		resetField();
		this.boneLookUp = boneLookUp;
	}

	@Override
	public void writeTo(BinaryWriter writer, Vertex vertex) {
		int a = vertex.getBoneIndex1();
		int b = vertex.getBoneIndex2();
		int c = vertex.getBoneIndex3();
		int d = vertex.getBoneIndex4();

		a = this.boneLookUp[a];
		b = b != 0 ? this.boneLookUp[b] : 0;
		c = c != 0 ? this.boneLookUp[c] : 0;
		d = d != 0 ? this.boneLookUp[d] : 0;

		writer.writeInt16(a);
		writer.writeInt16(b);
		writer.writeInt16(c);
		writer.writeInt16(d);

		this.min[0] = Math.min(this.min[0], a & 0xFFFFF);
		this.min[1] = Math.min(this.min[1], b & 0xFFFFF);
		this.min[2] = Math.min(this.min[2], c & 0xFFFFF);
		this.min[3] = Math.min(this.min[3], d & 0xFFFFF);
		this.max[0] = Math.max(this.max[0], a & 0xFFFFF);
		this.max[1] = Math.max(this.max[1], b & 0xFFFFF);
		this.max[2] = Math.max(this.max[2], c & 0xFFFFF);
		this.max[3] = Math.max(this.max[3], d & 0xFFFFF);
	}

	@Override
	public void resetField() {
		super.resetField();

		this.min = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
		this.max = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };
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