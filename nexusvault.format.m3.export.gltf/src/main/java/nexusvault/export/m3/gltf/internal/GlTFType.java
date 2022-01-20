package nexusvault.export.m3.gltf.internal;

public enum GlTFType {
	SCALAR("SCALAR", 1),
	VEC2("VEC2", 2),
	VEC3("VEC3", 3),
	VEC4("VEC4", 4),
	MAT2("MAT2", 4),
	MAT3("MAT3", 9),
	MAT4("MAT4", 16);

	private final String id;
	private final int componentCount;

	private GlTFType(String id, int componentCount) {
		this.id = id;
		this.componentCount = componentCount;
	}

	public String getId() {
		return this.id;
	}

	public int getComponentCount() {
		return this.componentCount;
	}
}