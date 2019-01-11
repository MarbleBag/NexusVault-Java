package nexusvault.format.tex;

/**
 * Indicates how this channel is used in rendering <br>
 * <b>Note</b>: In case the usage of a channel is not uniform across all texture files and depends on the material used, this enum will be removed in a further
 * update
 */
public enum TextureChannelType {
	DIFFUSE,
	NORMAL,
	METALLIC,
	ROUGHNESS,
	EMISSION,
	UNKNOWN
}
