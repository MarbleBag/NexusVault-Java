package nexusvault.export.m3.gltf.internal;

public enum GlTFMeshAttribute {
	POSITION("POSITION", 1),
	TEXCOORD("TEXCOORD", 2),
	JOINTS("JOINTS_0", 1),
	WEIGHTS("WEIGHTS_0", 1);

	private final String id;
	private final int maxAttributeCount;

	private GlTFMeshAttribute(String id, int maxAttributeCount) {
		this.id = id;
		this.maxAttributeCount = maxAttributeCount;
	}

	public String getAttributeKey() {
		return this.id;
	}
}