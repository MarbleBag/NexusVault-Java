package nexusvault.format.m3.v100;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import kreed.io.util.ByteBufferUtil;
import nexusvault.format.m3.ModelGeometry;
import nexusvault.format.m3.ModelMesh;
import nexusvault.format.m3.ModelVertex;
import nexusvault.format.m3.ModelVertexReader;
import nexusvault.format.m3.VertexField;
import nexusvault.format.m3.v100.struct.StructGeometry;
import nexusvault.format.m3.v100.struct.StructMesh;

/**
 * Internal implementation. May change without notice.
 */
final class InMemoryModelGeometry implements ModelGeometry {

	private final InMemoryModel model;
	private final StructGeometry struct;
	private final ModelVertexBuilder vertexReader;

	public InMemoryModelGeometry(InMemoryModel model, StructGeometry geometry) {
		this.model = model;
		this.struct = geometry;
		this.vertexReader = new ModelVertexBuilder(getStructGeometry());
	}

	protected InMemoryModel getModel() {
		return this.model;
	}

	protected StructGeometry getStructGeometry() {
		return this.struct;
	}

	@Override
	public int getMeshCount() {
		return getStructGeometry().meshes.getArraySize();
	}

	@Override
	public List<ModelMesh> getMeshes() {
		return this.model.getAllStructsPacked(this.struct.meshes, (idx, struct) -> new InMemoryModelMesh(idx, struct, this));
	}

	@Override
	public ModelMesh getMesh(int idx) {
		final StructMesh struct = this.model.getStruct(getStructGeometry().meshes, idx);
		return new InMemoryModelMesh(idx, struct, this);
	}

	@Override
	public boolean hasVertexLocation() {
		return isVertexFieldAvailable(VertexField.LOCATION_B) || isVertexFieldAvailable(VertexField.LOCATION_A);
	}

	@Override
	public boolean hasVertexUnknownData1() {
		return isVertexFieldAvailable(VertexField.FIELD_3_UNK_1);
	}

	@Override
	public boolean hasVertexUnknownData2() {
		return isVertexFieldAvailable(VertexField.FIELD_3_UNK_2);
	}

	@Override
	public boolean hasVertexUnknownData3() {
		return isVertexFieldAvailable(VertexField.FIELD_3_UNK_3);
	}

	@Override
	public boolean hasVertexBoneIndices() {
		return isVertexFieldAvailable(VertexField.BONE_MAP);
	}

	@Override
	public boolean hasVertexBoneWeights() {
		return isVertexFieldAvailable(VertexField.BONE_WEIGHTS);
	}

	@Override
	public boolean hasVertexUnknownData4() {
		return isVertexFieldAvailable(VertexField.FIELD_4_UNK_1);
	}

	@Override
	public boolean hasVertexUnknownData5() {
		return isVertexFieldAvailable(VertexField.FIELD_4_UNK_2);
	}

	@Override
	public boolean hasVertex1TextureCoords() {
		return isVertexFieldAvailable(VertexField.UV_MAP_1) && !isVertexFieldAvailable(VertexField.UV_MAP_2);
	}

	@Override
	public boolean hasVertex2TextureCoords() {
		return isVertexFieldAvailable(VertexField.UV_MAP_2);
	}

	@Override
	public boolean hasVertexUnknownData6() {
		return isVertexFieldAvailable(VertexField.FIELD_6_UNK_1);
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
		final BytePositionTracker memory = this.model.getMemory();
		memory.setPosition(computeVertexIndexOffset(indexOffset));
		final int[] indices = new int[(int) count];
		for (int i = 0; i < count; ++i) {
			indices[i] = ByteBufferUtil.getUInt16(memory.getData());
		}
		return indices;
	}

	protected long computeVertexOffset(long vertexIndex) {
		final long memoryOffset = this.struct.vertexBlockData.getOffset();
		final long vertexOffset = memoryOffset + vertexIndex * this.struct.vertexBlockSizeInBytes;
		return vertexOffset;
	}

	protected long computeVertexIndexOffset(long indexIndex) {
		return this.struct.indexData.getOffset() + indexIndex * 2 /* 2 bytes size */;
	}

	protected ModelVertex getVertex(long vertexIndex) {
		final long vertexStart = computeVertexOffset(vertexIndex);
		final BytePositionTracker memory = this.model.getMemory();
		if (memory.getPosition() != vertexStart) {
			memory.setPosition(vertexStart);
		}
		return this.vertexReader.read(memory);
	}

	protected List<ModelVertex> getVertices(long startVertex, long vertexCount) {
		final long vertexStart = computeVertexOffset(startVertex);
		final BytePositionTracker memory = this.model.getMemory();
		if (memory.getPosition() != vertexStart) {
			memory.setPosition(vertexStart);
		}
		final List<ModelVertex> result = new ArrayList<>((int) vertexCount);
		for (int i = 0; i < vertexCount; ++i) {
			result.add(this.vertexReader.read(memory));
		}
		return result;
	}

	@Override
	public Set<VertexField> getAvailableVertexFields() {
		return getStructGeometry().getAvailableVertexFields();
	}

	@Override
	public boolean isVertexFieldAvailable(VertexField field) {
		return getStructGeometry().isVertexFieldAvailable(field);
	}

	public ModelVertexReader getVertexReader(StructMesh mesh) {
		return new InMemoryVertexReader(getStructGeometry(), mesh, getModel().getMemory());
	}

	private interface ModelVertexIterator extends Iterator<ModelVertex> {
		boolean hasPrevious();

		ModelVertex previous();
	}

	@Deprecated
	protected Iterator<ModelVertex> buildIterator(int startIdx, long startVertex, long vertexCount) {
		final long vertexDataOffset = this.struct.vertexBlockData.getOffset();

		return new ModelVertexIterator() {
			private final long vertexSize = InMemoryModelGeometry.this.struct.vertexBlockSizeInBytes;
			private final long vertexStart = vertexDataOffset + startVertex * this.vertexSize;

			private long idx = startIdx;

			@Override
			public boolean hasNext() {
				return this.idx < vertexCount;
			}

			@Override
			public boolean hasPrevious() {
				return this.idx > 0;
			}

			@Override
			public ModelVertex next() {
				if (this.idx >= vertexCount) {
					throw new IndexOutOfBoundsException();
				}
				final ModelVertex vertex = getVertex();
				this.idx += 1;
				return vertex;
			}

			@Override
			public ModelVertex previous() {
				if (--this.idx < 0) {
					throw new IndexOutOfBoundsException();
				}
				final ModelVertex vertex = getVertex();
				return vertex;
			}

			private ModelVertex getVertex() {
				final BytePositionTracker memory = InMemoryModelGeometry.this.model.getMemory();
				final long nPos = this.vertexStart + this.idx * this.vertexSize;
				if (memory.getPosition() != nPos) {
					memory.setPosition(nPos);
				}
				return InMemoryModelGeometry.this.vertexReader.read(memory);
			}

		};
	}

}
