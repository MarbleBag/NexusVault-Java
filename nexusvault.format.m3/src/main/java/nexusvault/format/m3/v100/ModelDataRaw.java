package nexusvault.format.m3.v100;

import kreed.io.util.ByteBufferUtil;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.ModelData;
import nexusvault.format.m3.v100.pointer.ArrayTypePointer;
import nexusvault.format.m3.v100.struct.StructGeometry;
import nexusvault.format.m3.v100.struct.StructM3Header;
import nexusvault.format.m3.v100.struct.StructSubMesh;
import nexusvault.format.m3.v100.struct.StructTexture;

public class ModelDataRaw implements ModelData {

	public static class SubMesh {
		public final float[] vertices;
		public final float[] normals;
		public final float[][] uvs;
		public final int[] indices;

		public SubMesh(float[] vertices, float[][] uvs, float[] normals, int[] indices) {
			this.vertices = vertices;
			this.normals = normals;
			this.uvs = uvs;
			this.indices = indices;
		}
	}

	public static class Mesh {
		public final SubMesh[] submeshes;

		public Mesh(SubMesh[] submeshes) {
			this.submeshes = submeshes;
		}
	}

	public final StructM3Header header;
	public final DataTracker bufferReader;

	public ModelDataRaw(StructM3Header header, DataTracker bufferReader) {
		this.header = header;
		this.bufferReader = bufferReader;
	}

	@SuppressWarnings("unchecked")
	private <T> T getStruct(ArrayTypePointer pointer, int idx) {
		if ((idx < 0) || (pointer.getArraySize() <= idx)) {
			throw new IndexOutOfBoundsException();
		}
		final int position = (int) (pointer.getOffset() + (pointer.getElementSize() * idx));
		this.bufferReader.setPosition(position);
		return (T) StructUtil.readStruct(pointer.getTypeOfElement(), this.bufferReader.getData(), false);
	}

	private StructGeometry getGeometry() {
		return getStruct(header.geometry, 0);
	}

	private StructSubMesh getSubMesh(StructGeometry geometry, int idx) {
		if ((idx < 0) || (geometry.submeshes.getArraySize() <= idx)) {
			throw new IndexOutOfBoundsException();
		}
		return getStruct(geometry.submeshes, idx);
	}

	public StructSubMesh getMesh(int idx) {
		return getSubMesh(getGeometry(), idx);
	}

	public StructTexture getTexture(int idx) {
		if ((idx < 0) || (getTextureCount() <= idx)) {
			throw new IndexOutOfBoundsException();
		}
		final StructTexture texture = getStruct(header.textures, idx);
		texture.setName(bufferReader);
		return texture;
	}

	public int getMeshCount() {
		return getGeometry().submeshes.getArraySize();
	}

	public int getTextureCount() {
		return header.textures.getArraySize();
	}

	public SubMesh loadSubmesh(int idx) {
		final StructGeometry geometry = getGeometry();
		final StructSubMesh subMesh = getSubMesh(geometry, idx);

		final int checkNumberOfUVMaps = getNumberOfUVMaps(geometry);

		final float[] vertices = new float[(int) (subMesh.numberOfVertices * 3)];
		final float[] normals = new float[(int) (subMesh.numberOfVertices * 3)];
		final float[][] uvs = new float[checkNumberOfUVMaps][(int) (subMesh.numberOfVertices * 2)];
		final int[] indices = new int[(int) (subMesh.numberOfIndices)];

		final int vertexBlockSize = geometry.vertexBlockSizeInBytes;
		final long vertexBlockStart = geometry.vertexBlockData.getOffset() + (subMesh.startVertexBlock * vertexBlockSize);
		final float scale = (Short.MAX_VALUE / 64f); // Short.toUnsignedInt((short) -1);

		for (int i = 0; i < subMesh.numberOfVertices; ++i) {
			final long positionBlock = (vertexBlockStart + (i * vertexBlockSize));
			// bufferReader.setPosition(positionBlock);

			loadPosition(vertices, i, bufferReader, positionBlock, geometry);
			loadUVMap(uvs, i, bufferReader, positionBlock, geometry);
		}

		bufferReader.setPosition(geometry.indexData.getOffset() + (subMesh.startIndex * 2));
		for (int i = 0; i < subMesh.numberOfIndices; ++i) {
			indices[i] = ByteBufferUtil.getUInt16(bufferReader.getData());
		}

		return new SubMesh(vertices, uvs, normals, indices);
	}

	private int getNumberOfUVMaps(StructGeometry geometry) {
		int acc = 0;
		for (int i = geometry.getVertexBlockFieldCount() - 1; i >= 0; --i) {
			if (geometry.isVertexBlockFieldUsed(i)) {
				final int type = geometry.getVertexBlockFieldType(i);
				if (type == 5) {
					acc += 1;
				} else {
					break;
				}
			}
		}
		return acc;
	}

	private void loadPosition(float[] vertices, int arrayIdx, DataTracker bufferReader, long positionBlock, StructGeometry geometry) {
		final float scale = (Short.MAX_VALUE / 64f);
		for (int fieldIndex = 0; fieldIndex < geometry.getVertexBlockFieldCount(); ++fieldIndex) {
			if (!geometry.isVertexBlockFieldUsed(fieldIndex)) {
				continue;
			}
			final int fieldType = geometry.getVertexBlockFieldType(fieldIndex);
			final int[] fieldRange = geometry.getVertexBlockFieldPosition(fieldIndex);

			final long currentPosition = positionBlock + fieldRange[0];
			final long expectedPosition = positionBlock + fieldRange[1];

			bufferReader.setPosition(currentPosition);

			if (1 == fieldType) {
				vertices[(arrayIdx * 3) + 0] = (ByteBufferUtil.getFloat32(bufferReader.getData()));
				vertices[(arrayIdx * 3) + 1] = (ByteBufferUtil.getFloat32(bufferReader.getData()));
				vertices[(arrayIdx * 3) + 2] = (ByteBufferUtil.getFloat32(bufferReader.getData()));
				return;
			} else if (2 == fieldType) {
				vertices[(arrayIdx * 3) + 0] = (ByteBufferUtil.getInt16(bufferReader.getData()) / scale);
				vertices[(arrayIdx * 3) + 1] = (ByteBufferUtil.getInt16(bufferReader.getData()) / scale);
				vertices[(arrayIdx * 3) + 2] = (ByteBufferUtil.getInt16(bufferReader.getData()) / scale);
				return;
			}
		}
	}

	private void loadUVMap(float[][] uvs, int arrayIdx, DataTracker bufferReader, long positionBlock, StructGeometry geometry) {
		int uvsIdx = uvs.length;
		for (int fieldIndex = 0; fieldIndex < geometry.getVertexBlockFieldCount(); ++fieldIndex) {
			if (!geometry.isVertexBlockFieldUsed(fieldIndex)) {
				continue;
			}
			final int fieldType = geometry.getVertexBlockFieldType(fieldIndex);
			final int[] fieldRange = geometry.getVertexBlockFieldPosition(fieldIndex);

			if (fieldType != 5) {
				continue;
			}

			final long currentPosition = positionBlock + fieldRange[0];
			final long expectedPosition = positionBlock + fieldRange[1];

			bufferReader.setPosition(currentPosition);

			final float[] uv = uvs[--uvsIdx];
			uv[(arrayIdx * 2) + 0] = ByteBufferUtil.getFloat16(bufferReader.getData());
			uv[(arrayIdx * 2) + 1] = 1f - ByteBufferUtil.getFloat16(bufferReader.getData());

			if (uvsIdx <= 0) {
				break;
			}
		}
	}

	private int getSubmeshCount(StructGeometry geometry) {
		return geometry.submeshes.getArraySize();
	}

	public Mesh getMesh() {
		final StructGeometry geometry = getGeometry();
		final SubMesh[] submeshes = new SubMesh[getSubmeshCount(geometry)];
		for (int i = 0; i < submeshes.length; ++i) {
			submeshes[i] = loadSubmesh(i);
		}
		return new Mesh(submeshes);
	}

}
