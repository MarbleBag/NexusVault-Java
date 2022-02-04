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

import java.util.LinkedList;

import kreed.io.util.BinaryWriter;
import nexusvault.export.m3.gltf.internal.GlTFComponentType;
import nexusvault.format.m3.Mesh;
import nexusvault.format.m3.Model;

public final class MeshWriter {

	private final VertexFieldWriter[] fieldWriter;
	private final int vertexSizeInBytes;

	private int minIdxValue;
	private int maxIdxValue;

	public MeshWriter(Model model) {
		this.fieldWriter = computeVertex(model);

		var writeSize = 0;
		for (final var accessor : this.fieldWriter) {
			writeSize += accessor.getSizeInBytes();
		}

		this.vertexSizeInBytes = writeSize;
	}

	public VertexFieldWriter[] getVertexFields() {
		return this.fieldWriter;
	}

	public int getVertexWriteSize() {
		return this.vertexSizeInBytes;
	}

	public int getIndexUpperBound() {
		return this.maxIdxValue;
	}

	public int getIndexLowerBound() {
		return this.minIdxValue;
	}

	private VertexFieldWriter[] computeVertex(Model model) {
		final var geometry = model.getGeometry();
		final LinkedList<VertexFieldWriter> vertexFields = new LinkedList<>();

		if (geometry.hasVertexLocation()) {
			final var offset = getOffsetWithinVertex(vertexFields);
			final var field = new VFWPosition(offset);
			vertexFields.add(field);
		}

		if (geometry.hasVertex1TextureCoords() || geometry.hasVertex2TextureCoords()) {
			final var offset = getOffsetWithinVertex(vertexFields);
			final var field = new VFWTexCoords1(offset);
			vertexFields.add(field);
		}

		if (geometry.hasVertex2TextureCoords()) {
			final var offset = getOffsetWithinVertex(vertexFields);
			final var field = new VFWTexCoords2(offset);
			vertexFields.add(field);
		}

		if (geometry.hasVertexBoneIndices()) {
			final var offset = getOffsetWithinVertex(vertexFields);
			final var field = new VFWBoneIndices(offset, model.getBoneLookUp());
			vertexFields.add(field);
		}

		if (geometry.hasVertexBoneWeights()) {
			final var offset = getOffsetWithinVertex(vertexFields);
			final var field = new VFWBoneWeights(offset);
			vertexFields.add(field);
		}

		return vertexFields.toArray(new VertexFieldWriter[vertexFields.size()]);
	}

	private int getOffsetWithinVertex(LinkedList<VertexFieldWriter> vertexFields) {
		int offset = 0;
		if (!vertexFields.isEmpty()) {
			final VertexFieldWriter lastField = vertexFields.getLast();
			offset = lastField.getFieldOffset() + lastField.getSizeInBytes();
		}
		return offset;
	}

	public void writeGeometry(Mesh mesh, BinaryWriter writer) {
		for (final var accessors : this.fieldWriter) {
			accessors.resetField();
		}

		for (final var vertex : mesh.getVertices()) {
			for (final var accessors : this.fieldWriter) {
				accessors.writeTo(writer, vertex);
			}
		}
	}

	public void writeIndices(Mesh mesh, BinaryWriter writer) {
		this.minIdxValue = Integer.MAX_VALUE;
		this.maxIdxValue = 0;

		for (final var index : mesh.getIndices()) {
			switch (GlTFComponentType.UINT32) {
				case UINT32:
					writer.writeInt32(index);
					break;
				case UINT16:
					writer.writeInt16(index);
					break;
				default:
					throw new IllegalStateException("TODO"); // TODO
			}

			this.minIdxValue = Math.min(this.minIdxValue, index);
			this.maxIdxValue = Math.max(this.maxIdxValue, index);
		}
	}
}
