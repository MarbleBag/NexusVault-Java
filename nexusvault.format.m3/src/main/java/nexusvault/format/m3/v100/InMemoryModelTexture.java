package nexusvault.format.m3.v100;

import nexusvault.format.m3.ModelTexture;
import nexusvault.format.m3.v100.struct.StructTexture;

class InMemoryModelTexture implements ModelTexture {

	private final StructTexture texture;
	private final InMemoryModel model;

	private String name;

	public InMemoryModelTexture(StructTexture texture, InMemoryModel model) {
		super();
		this.texture = texture;
		this.model = model;
	}

	@Override
	public String getTexturePath() {
		if (name == null) {
			name = texture.getName(model.getMemory());
		}
		return name;
	}

	@Override
	public TextureType getTextureType() {
		switch (texture.textureType) {
			case 0:
				return TextureType.DIFFUSE;
			case 1:
				return TextureType.NORMAL;
			case 2:
			default:
				return TextureType.UNKNOWN;
		}
	}

}
