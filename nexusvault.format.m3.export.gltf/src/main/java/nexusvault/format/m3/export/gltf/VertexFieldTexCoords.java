package nexusvault.format.m3.export.gltf;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

abstract class VertexFieldTexCoords extends VertexField {
	private final int idx;

	VertexFieldTexCoords(int offsetWithinVertex, int idx) {
		super("TexCoord" + idx, GlTFComponentType.FLOAT, GlTFType.VEC2, GlTFMeshAttribute.TEXCOORD, offsetWithinVertex);
		this.idx = idx;
		resetField();
	}

	@Override
	public void writeTo(BinaryWriter writer, ModelVertex vertex) {
		final float u = vertex.getTextureCoordU1();
		final float v = vertex.getTextureCoordV1();
		writer.writeFloat32(u);
		writer.writeFloat32(v);
	}

	@Override
	public String getAttributeKey() {
		return super.getAttributeKey() + "_" + idx;
	}
}