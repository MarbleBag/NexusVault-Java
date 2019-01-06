package nexusvault.format.m3.export.gltf;

enum GlTFMeshAttribute {
	POSITION("POSITION", 1),
	TEXCOORD("TEXCOORD", 2);

	private final String id;
	private final int maxAttributeCount;

	private GlTFMeshAttribute(String id, int maxAttributeCount) {
		this.id = id;
		this.maxAttributeCount = maxAttributeCount;
	}

	public String getAttributeKey() {
		return id;
	}
}