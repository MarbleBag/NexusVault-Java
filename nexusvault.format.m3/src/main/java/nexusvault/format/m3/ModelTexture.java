package nexusvault.format.m3;

public interface ModelTexture {

	/**
	 * Names are not final. Each texture can contain layers with additional informations for the used shader.
	 */
	public static enum TextureType {
		UNKNOWN,
		DIFFUSE,
		NORMAL
	}

	String getTexturePath();

	TextureType getTextureType();
}
