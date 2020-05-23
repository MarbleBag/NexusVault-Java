package nexusvault.format.m3.export.gltf.internal.vertex;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

public final class VFWTexCoords1 extends VFRTexCoords {

	public VFWTexCoords1(int offsetWithinVertex) {
		super(offsetWithinVertex, 0);
	}

	@Override
	public void writeTo(BinaryWriter writer, ModelVertex vertex) {
		final float u = vertex.getTextureCoordU1();
		final float v = vertex.getTextureCoordV1();
		writer.writeFloat32(u);
		writer.writeFloat32(v);
	}

}