package nexusvault.format.m3.export.gltf.internal;

public enum GlTFMode {
	POINTS,
	LINES,
	LINE_LOOP,
	LINE_STRIP,
	TRIANGLES,
	TRIANGLE_STRIP,
	TRIANGLE_FAN;
	public int getId() {
		return ordinal();
	}
}