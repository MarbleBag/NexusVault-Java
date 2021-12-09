package nexusvault.format.m3.v100;

import java.util.Iterator;
import java.util.List;

import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.ModelVertex;
import nexusvault.format.m3.ModelVertexReader;
import nexusvault.format.m3.v100.struct.StructMesh;

/**
 * Internal implementation. May change without notice.
 */
final class InMemoryModelMesh implements ModelMesh {

	private final int meshNo;
	private final StructMesh structMesh;
	private final InMemoryModelGeometry model;

	public InMemoryModelMesh(int meshNo, StructMesh structMesh, InMemoryModelGeometry inMemoryGeometry) {
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
	public ModelVertex getVertex(int idx) {
		if (idx < 0 || getVertexCount() <= idx) {
			throw new IndexOutOfBoundsException(String.format("Idx out of range. Allowed range is [0,%d)", getVertexCount()));
		}
		return this.model.getVertex(this.structMesh.startVertex + idx);
	}

	@Override
	public List<ModelVertex> getVertices() {
		return this.model.getVertices(this.structMesh.startVertex, this.structMesh.vertexCount);
	}

	@Override
	public ModelVertexReader getVertexReader() {
		return this.model.getVertexReader(this.structMesh);
	}

	@Override
	@Deprecated
	public Iterable<ModelVertex> iterateVertices() {
		return () -> buildIterator(0);
	}

	private Iterator<ModelVertex> buildIterator(int startIdx) {
		return this.model.buildIterator(startIdx, this.structMesh.startVertex, this.structMesh.vertexCount);
	}

}
