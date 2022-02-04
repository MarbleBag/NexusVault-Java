package nexusvault.export.m3.gltf.internal.vertex;

import nexusvault.export.m3.gltf.internal.GlTFComponentType;
import nexusvault.export.m3.gltf.internal.GlTFMeshAttribute;
import nexusvault.export.m3.gltf.internal.GlTFType;

public abstract class VFRTexCoords extends VertexFieldWriter {
	private final int idx;

	public VFRTexCoords(int offsetWithinVertex, int idx) {
		super("TexCoord" + idx, GlTFComponentType.FLOAT, GlTFType.VEC2, GlTFMeshAttribute.TEXCOORD, offsetWithinVertex);
		this.idx = idx;
		resetField();
	}

	@Override
	public String getAttributeKey() {
		return super.getAttributeKey() + "_" + this.idx;
	}
}