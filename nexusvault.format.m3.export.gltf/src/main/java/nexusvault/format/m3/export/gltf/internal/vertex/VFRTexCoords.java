package nexusvault.format.m3.export.gltf.internal.vertex;

import nexusvault.format.m3.export.gltf.internal.GlTFComponentType;
import nexusvault.format.m3.export.gltf.internal.GlTFMeshAttribute;
import nexusvault.format.m3.export.gltf.internal.GlTFType;

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