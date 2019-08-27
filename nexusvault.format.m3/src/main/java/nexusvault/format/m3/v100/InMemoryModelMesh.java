package nexusvault.format.m3.v100;

import java.util.Iterator;
import java.util.List;

import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.ModelVertex;
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
		model = inMemoryGeometry;
	}

	@Override
	public int getMaterialReference() {
		return structMesh.materialSelector;
	}

	@Override
	public int getMeshIndex() {
		return meshNo;
	}

	@Override
	public boolean hasMeshGroup() {
		return structMesh.meshGroupId != -1;
	}

	@Override
	public int getMeshGroup() {
		return 0xFF & structMesh.meshGroupId;
	}

	@Override
	public int getMeshToBodyPart() {
		return 0xFF & structMesh.meshAnatomyId;
	}

	@Override
	public long getVertexCount() {
		return structMesh.vertexCount;
	}

	@Override
	public long getIndexCount() {
		return structMesh.indexCount;
	}

	@Override
	public int[] getIndices() {
		return model.getIndices(structMesh.startIndex, structMesh.indexCount);
	}

	@Override
	public ModelVertex getVertex(int idx) {
		if ((idx < 0) || (getVertexCount() <= idx)) {
			throw new IndexOutOfBoundsException(String.format("Idx out of range. Allowed range is [0,%d)", getVertexCount()));
		}
		return model.getVertex(structMesh.startVertex + idx);
	}

	@Override
	public List<ModelVertex> getVertices() {
		return model.getVertices(structMesh.startVertex, structMesh.vertexCount);
	}

	@Override
	@Deprecated
	public Iterable<ModelVertex> iterateVertices() {
		return () -> buildIterator(0);
	}

	private Iterator<ModelVertex> buildIterator(int startIdx) {
		return model.buildIterator(startIdx, structMesh.startVertex, structMesh.vertexCount);
	}

}
