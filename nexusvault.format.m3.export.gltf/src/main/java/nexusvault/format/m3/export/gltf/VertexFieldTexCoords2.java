package nexusvault.format.m3.export.gltf;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

final class VertexFieldTexCoords2 extends VertexFieldTexCoords {

	VertexFieldTexCoords2(int offsetWithinVertex) {
		super(offsetWithinVertex, 1);
	}

	@Override
	public void writeTo(BinaryWriter writer, ModelVertex vertex) {
		final float u = vertex.getTextureCoordU2();
		final float v = vertex.getTextureCoordV2();
		writer.writeFloat32(u);
		writer.writeFloat32(v);
	}

}