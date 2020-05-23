package nexusvault.format.m3.export.gltf.internal.vertex;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

public final class VFWTexCoords2 extends VFRTexCoords {

	public VFWTexCoords2(int offsetWithinVertex) {
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