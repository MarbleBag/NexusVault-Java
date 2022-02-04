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

public final class VFWPosition extends VertexFieldWriter {

	private float[] min;
	private float[] max;

	public VFWPosition(int offsetWithinVertex) {
		super("Pos", GlTFComponentType.FLOAT, GlTFType.VEC3, GlTFMeshAttribute.POSITION, offsetWithinVertex);
		resetField();
	}

	@Override
	public void writeTo(BinaryWriter writer, Vertex vertex) {
		final float x = vertex.getLocationX();
		final float y = vertex.getLocationY();
		final float z = vertex.getLocationZ();
		writer.writeFloat32(x);
		writer.writeFloat32(y);
		writer.writeFloat32(z);

		this.min[0] = Math.min(this.min[0], x);
		this.min[1] = Math.min(this.min[1], y);
		this.min[2] = Math.min(this.min[2], z);
		this.max[0] = Math.max(this.max[0], x);
		this.max[1] = Math.max(this.max[1], y);
		this.max[2] = Math.max(this.max[2], z);
	}

	@Override
	public void resetField() {
		super.resetField();

		this.min = new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
		this.max = new float[] { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
	}

	@Override
	public boolean hasMinimum() {
		return true;
	}

	@Override
	public boolean hasMaximum() {
		return true;
	}

	@Override
	public Number[] getMinimum() {
		return new Number[] { this.min[0], this.min[1], this.min[2] };
	}

	@Override
	public Number[] getMaximum() {
		return new Number[] { this.max[0], this.max[1], this.max[2] };
	}
}