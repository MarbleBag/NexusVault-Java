package nexusvault.format.m3.impl;

import java.util.Iterator;
import java.util.List;

import nexusvault.format.m3.Mesh;
import nexusvault.format.m3.Vertex;
import nexusvault.format.m3.VertexReader;
import nexusvault.format.m3.struct.StructMesh;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryMesh implements Mesh {

	private final int meshNo;
	private final StructMesh structMesh;
	private final InMemoryGeometry model;

	public InMemoryMesh(int meshNo, StructMesh structMesh, InMemoryGeometry inMemoryGeometry) {
		this.meshNo = meshNo;
		this.structMesh = structMesh;
		this.model = inMemoryGeometry;
	}

	@Override
	public int getMaterialReference() {
		return this.structMesh.materialSelector;
	}

	@Override
	public int getMeshIndex() {
		return this.meshNo;
	}

	@Override
	public boolean hasMeshGroup() {
		return this.structMesh.meshGroupId != -1;
	}

	@Override
	public int getMeshGroup() {
		return 0xFF & this.structMesh.meshGroupId;
	}

	@Override
	public int getMeshToBodyPart() {
		return 0xFF & this.structMesh.modelClusterId;
	}

	@Override
	public long getVertexCount() {
		return this.structMesh.vertexCount;
	}

	@Override
	public long getIndexCount() {
		return this.structMesh.indexCount;
	}

	@Override
	public int[] getIndices() {
		return this.model.getIndices(this.structMesh.startIndex, this.structMesh.indexCount);
	}

	@Override
	public Vertex getVertex(int idx) {
		if (idx < 0 || getVertexCount() <= idx) {
			throw new IndexOutOfBoundsException(String.format("Idx out of range. Allowed range is [0,%d)", getVertexCount()));
		}
		return this.model.getVertex(this.structMesh.startVertex + idx);
	}

	@Override
	public List<Vertex> getVertices() {
		return this.model.getVertices(this.structMesh.startVertex, this.structMesh.vertexCount);
	}

	@Override
	public VertexReader getVertexReader() {
		return this.model.getVertexReader(this.structMesh);
	}

	@Override
	@Deprecated
	public Iterable<Vertex> iterateVertices() {
		return () -> buildIterator(0);
	}

	private Iterator<Vertex> buildIterator(int startIdx) {
		return this.model.buildIterator(startIdx, this.structMesh.startVertex, this.structMesh.vertexCount);
	}

}
