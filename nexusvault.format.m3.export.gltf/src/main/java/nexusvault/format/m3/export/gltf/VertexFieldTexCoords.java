package nexusvault.format.m3.export.gltf;

abstract class VertexFieldTexCoords extends VertexField {
	private final int idx;

	VertexFieldTexCoords(int offsetWithinVertex, int idx) {
		super("TexCoord" + idx, GlTFComponentType.FLOAT, GlTFType.VEC2, GlTFMeshAttribute.TEXCOORD, offsetWithinVertex);
		this.idx = idx;
		resetField();
	}

	@Override
	public String getAttributeKey() {
		return super.getAttributeKey() + "_" + idx;
	}
}