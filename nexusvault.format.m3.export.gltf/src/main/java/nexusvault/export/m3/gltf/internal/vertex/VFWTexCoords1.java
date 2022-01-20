package nexusvault.export.m3.gltf.internal.vertex;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.Vertex;

public final class VFWTexCoords1 extends VFRTexCoords {

	public VFWTexCoords1(int offsetWithinVertex) {
		super(offsetWithinVertex, 0);
	}

	@Override
	public void writeTo(BinaryWriter writer, Vertex vertex) {
		final float u = vertex.getTextureCoordU1();
		final float v = vertex.getTextureCoordV1();
		writer.writeFloat32(u);
		writer.writeFloat32(v);
	}

}