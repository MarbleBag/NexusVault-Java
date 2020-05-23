package nexusvault.format.m3.v100;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kreed.io.util.ByteBufferUtil;
import nexusvault.format.m3.ModelVertex;
import nexusvault.format.m3.ModelVertexReader;
import nexusvault.format.m3.VertexField;
import nexusvault.format.m3.v100.struct.StructGeometry;
import nexusvault.format.m3.v100.struct.StructMesh;

// TODO not good enough
final class InMemoryVertexReader implements ModelVertexReader {

	private static class DefaultField implements ModelVertexField {

		private final VertexField field;
		private final int position;

		public DefaultField(VertexField field, int position) {
			this.field = field;
			this.position = position;
		}

		@Override
		public VertexField type() {
			return this.field;
		}

		@Override
		public int position() {
			return this.position;
		}

		@Override
		public int length() {
			return this.field.getSizeInBytes();
		}

	}

	private final ModelVertexField[] fields;
	private final int vertexSize;
	private final int vertexCount;
	private final long memoryStart;
	private final long memoryEnd;
	private long vertexOffset;
	private final BytePositionTracker memory;
	private final ModelVertexBuilder vertexBuilder;

	private final byte[] store;

	public InMemoryVertexReader(StructGeometry geometry, StructMesh mesh, BytePositionTracker memory) {
		this.fields = computeFields(geometry);
		this.vertexSize = geometry.vertexBlockSizeInBytes;
		this.vertexCount = (int) mesh.vertexCount;
		this.memoryStart = geometry.vertexBlockData.getOffset() + mesh.startVertex * this.vertexSize;
		this.memoryEnd = this.memoryStart + this.vertexSize * this.vertexCount;
		this.vertexOffset = this.memoryStart;
		this.memory = memory;
		this.store = new byte[this.vertexSize];
		this.vertexBuilder = new ModelVertexBuilder(geometry);
	}

	private ModelVertexField[] computeFields(StructGeometry geometry) {
		final var vertexFields = geometry.getAvailableVertexFields();
		final var fields = new ArrayList<ModelVertexField>(vertexFields.size());

		for (final var vertexField : vertexFields) {
			final var fieldPos = geometry.getVertexFieldPosition(vertexField);
			fields.add(new DefaultField(vertexField, fieldPos));
		}

		fields.sort((a, b) -> a.position() - b.position());
		return fields.toArray(n -> new ModelVertexField[n]);
	}

	@Override
	public List<ModelVertexField> getFields() {
		return Arrays.asList(this.fields);
	}

	@Override
	public int getVertexSizeInBytes() {
		return this.vertexSize;
	}

	@Override
	public int getVertexCount() {
		return this.vertexCount;
	}

	public boolean nextVertex() {
		this.vertexOffset += this.vertexSize;
		return this.vertexOffset < this.memoryEnd;
	}

	public boolean previousVertex() {
		this.vertexOffset -= this.vertexSize;
		return this.vertexOffset >= this.memoryStart;
	}

	@Override
	public void moveToVertex(int index) {
		final var position = this.memoryStart + this.vertexSize * index;
		if (position < this.memoryStart || this.memoryEnd < position) {
			throw new IllegalArgumentException();
		}
		this.vertexOffset = position;
	}

	private ByteBuffer setMemoryPosition(int additionaOffset) {
		if (this.memory.getPosition() != this.vertexOffset + additionaOffset) {
			this.memory.setPosition(this.vertexOffset + additionaOffset);
		}
		return this.memory.getData();
	}

	@Override
	public int[] readFieldInt(ModelVertexField field, int[] store, int offset) {
		final var elementCount = field.type().getNumberOfElements();
		final var elementType = field.type().getFormat();

		if (store == null) {
			store = new int[elementCount];
			offset = 0;
		}

		final var data = setMemoryPosition(field.position());
		for (var idx = 0; idx < elementCount; idx++) {
			switch (elementType) {
				case UINT8:
					store[idx + offset] = ByteBufferUtil.getUInt8(data);
					break;
				case INT16:
					store[idx + offset] = ByteBufferUtil.getInt16(data);
					break;
				case UINT16:
					store[idx + offset] = ByteBufferUtil.getUInt16(data);
					break;
				case INT32:
					store[idx + offset] = ByteBufferUtil.getInt32(data);
					break;
				case FLOAT16:
					store[idx + offset] = Math.round(ByteBufferUtil.getFloat16(data));
					break;
				case FLOAT32:
					store[idx + offset] = Math.round(ByteBufferUtil.getFloat32(data));
					break;
				default:
					throw new IllegalArgumentException(); // TODO
			}
		}

		return store;
	}

	@Override
	public float[] readFieldFloat(ModelVertexField field, float[] store, int offset) {
		final var elementCount = field.type().getNumberOfElements();
		final var elementType = field.type().getFormat();

		if (store == null) {
			store = new float[elementCount];
			offset = 0;
		}

		final var data = setMemoryPosition(field.position());
		for (var idx = 0; idx < elementCount; idx++) {
			switch (elementType) {
				case UINT8:
					store[idx + offset] = ByteBufferUtil.getUInt8(data);
					break;
				case INT16:
					store[idx + offset] = ByteBufferUtil.getInt16(data);
					break;
				case UINT16:
					store[idx + offset] = ByteBufferUtil.getUInt16(data);
					break;
				case INT32:
					store[idx + offset] = ByteBufferUtil.getInt32(data);
					break;
				case FLOAT16:
					store[idx + offset] = ByteBufferUtil.getFloat16(data);
					break;
				case FLOAT32:
					store[idx + offset] = ByteBufferUtil.getFloat32(data);
					break;
				default:
					throw new IllegalArgumentException(); // TODO
			}
		}

		return store;
	}

	@Override
	public ByteOrder getByteOrder() {
		return this.memory.getData().order();
	}

	@Override
	public byte[] readVertex(byte[] store, int offset) {
		final var elementCount = getVertexSizeInBytes();
		if (store == null) {
			store = new byte[elementCount];
			offset = 0;
		}
		final var data = setMemoryPosition(0);
		data.get(store, offset, elementCount);
		nextVertex();
		return store;
	}

	@Override
	public byte[] readVertex(byte[] store, int offset, List<ModelVertexField> fields) {
		if (store == null) {
			final var size = countBytes(fields);
			store = new byte[size];
			offset = 0;
		}
		final var tmpStore = readVertex(this.store, 0);
		for (final var field : fields) {
			System.arraycopy(tmpStore, field.position(), store, offset, field.length());
		}
		return store;
	}

	private int countBytes(List<ModelVertexField> fields) {
		var result = 0;
		for (final var field : fields) {
			result += field.length();
		}
		return result;
	}

	@Override
	public ModelVertex readVertex() {
		setMemoryPosition(0);
		nextVertex();
		return this.vertexBuilder.read(this.memory);
	}

}
