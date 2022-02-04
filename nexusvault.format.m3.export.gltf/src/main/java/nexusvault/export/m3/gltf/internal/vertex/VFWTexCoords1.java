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
import nexusvault.format.m3.Vertex;

public final class VFWTexCoords1 extends VFRTexCoords {

	public VFWTexCoords1(int offsetWithinVertex) {
		super(offsetWithinVertex, 0);
	}

	@Override
	public void writeTo(BinaryWriter writer, Vertex vertex) {
		final float u = vertex.getTextureCoordU1();
		final float v = vertex.getTextureCoordV1();
		writer.writeFloat32(u);
		writer.writeFloat32(v);
	}

}