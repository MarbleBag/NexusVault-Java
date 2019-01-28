package nexusvault.format.m3.v100;

import java.util.List;

import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.ModelVertex;
import nexusvault.format.m3.ModelVertexIterator;
import nexusvault.format.m3.v100.struct.StructMesh;

class InMemoryModelMesh implements ModelMesh {

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
		return structMesh.materialSelector;
	}

	@Override
	public int getMeshIndex() {
		return meshNo;
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
		return model.getVertex(structMesh.startVertex, idx);
	}

	@Override
	public List<ModelVertex> getVertices() {
		return model.getVertices(structMesh.startVertex, structMesh.vertexCount);
	}

	@Override
	public Iterable<ModelVertex> iterateVertices() {
		return () -> buildIterator(0);
	}

	private ModelVertexIterator buildIterator(int startIdx) {
		return model.buildIterator(startIdx, structMesh.startVertex, structMesh.vertexCount);
	}

}
