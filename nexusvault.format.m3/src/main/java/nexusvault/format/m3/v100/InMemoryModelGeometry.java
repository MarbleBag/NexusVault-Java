package nexusvault.format.m3.v100;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kreed.io.util.ByteBufferUtil;
import nexusvault.format.m3.ModelGeometry;
import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.ModelVertex;
import nexusvault.format.m3.v100.struct.StructGeometry;
import nexusvault.format.m3.v100.struct.StructGeometry.VertexField;
import nexusvault.format.m3.v100.struct.StructMesh;

/**
 * Internal implementation. May change without notice.
 */
final class InMemoryModelGeometry implements ModelGeometry {

	private final InMemoryModel model;
	private final StructGeometry struct;
	private final VertexReader vertexReader;

	public InMemoryModelGeometry(InMemoryModel model, StructGeometry geometry) {
		this.model = model;
		this.struct = geometry;
		this.vertexReader = new VertexReader(getStructGeometry());
	}

	protected InMemoryModel getModel() {
		return model;
	}

	private StructGeometry getStructGeometry() {
		return struct;
	}

	@Override
	public int getMeshCount() {
		return getStructGeometry().meshes.getArraySize();
	}

	@Override
	public List<ModelMesh> getMeshes() {
		return model.getAllStructsPacked(struct.meshes, (idx, struct) -> new InMemoryModelMesh(idx, struct, this));
	}

	@Override
	public ModelMesh getMesh(int idx) {
		final StructMesh struct = model.getStruct(getStructGeometry().meshes, idx);
		return new InMemoryModelMesh(idx, struct, this);
	}

	@Override
	public boolean hasVertexLocation() {
		return getStructGeometry().isVertexFieldAvailable(VertexField.LOCATION);
	}

	@Override
	public boolean hasVertex1TextureCoords() {
		return getStructGeometry().isVertexFieldAvailable(VertexField.UV_MAP_1) && !getStructGeometry().isVertexFieldAvailable(VertexField.UV_MAP_2);
	}

	@Override
	public boolean hasVertex2TextureCoords() {
		return getStructGeometry().isVertexFieldAvailable(VertexField.UV_MAP_2);
	}

	@Override
	public boolean hasVertexBoneIndices() {
		return getStructGeometry().isVertexFieldAvailable(VertexField.BONE_MAP);
	}

	@Override
	public boolean hasVertexBoneWeights() {
		return getStructGeometry().isVertexFieldAvailable(VertexField.BONE_WEIGHTS);
	}

	@Override
	public long getVertexCount() {
		return getStructGeometry().vertexBlockCount;
	}

	@Override
	public long getIndexCount() {
		return getStructGeometry().indexCount;
	}

	protected int[] getIndices(long indexOffset, long count) {
		final DataTracker memory = model.getMemory();
		memory.setPosition(struct.indexData.getOffset() + (indexOffset * 2));
		final int[] indices = new int[(int) count];
		for (int i = 0; i < count; ++i) {
			indices[i] = ByteBufferUtil.getUInt16(memory.getData());
		}
		return indices;
	}

	protected ModelVertex getVertex(long vertexOffset, long idx) {
		final long vertexDataOffset = struct.vertexBlockData.getOffset();
		final long vertexStart = vertexDataOffset + (idx * struct.vertexBlockSizeInBytes);
		final DataTracker memory = model.getMemory();
		if (memory.getPosition() != vertexStart) {
			memory.setPosition(vertexStart);
		}
		return vertexReader.read(memory);
	}

	protected List<ModelVertex> getVertices(long startVertex, long vertexCount) {
		final long vertexDataOffset = struct.vertexBlockData.getOffset();
		final long vertexStart = vertexDataOffset + (startVertex * struct.vertexBlockSizeInBytes);
		final DataTracker memory = model.getMemory();
		if (memory.getPosition() != vertexStart) {
			memory.setPosition(vertexStart);
		}
		final List<ModelVertex> result = new ArrayList<>((int) vertexCount);
		for (int i = 0; i < vertexCount; ++i) {
			result.add(vertexReader.read(memory));
		}
		return result;
	}

	@Deprecated
	protected Iterator<ModelVertex> buildIterator(int startIdx, long startVertex, long vertexCount) {
		final long vertexDataOffset = struct.vertexBlockData.getOffset();

		return new Iterator<ModelVertex>() {
			private final long vertexSize = struct.vertexBlockSizeInBytes;
			private final long vertexStart = vertexDataOffset + (startVertex * vertexSize);

			private long idx = startIdx;

			@Override
			public boolean hasNext() {
				return idx < vertexCount;
			}

			public boolean hasPrevious() {
				return idx > 0;
			}

			@Override
			public ModelVertex next() {
				if (idx >= vertexCount) {
					throw new IndexOutOfBoundsException();
				}
				final ModelVertex vertex = getVertex();
				idx += 1;
				return vertex;
			}

			public ModelVertex previous() {
				if (--idx < 0) {
					throw new IndexOutOfBoundsException();
				}
				final ModelVertex vertex = getVertex();
				return vertex;
			}

			private ModelVertex getVertex() {
				final DataTracker memory = model.getMemory();
				final long nPos = vertexStart + (idx * vertexSize);
				if (memory.getPosition() != nPos) {
					memory.setPosition(nPos);
				}
				return vertexReader.read(memory);
			}

		};
	}

}
