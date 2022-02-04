package nexusvault.format.m3;

import java.util.List;

public interface Model {

	public static Model read(byte[] data) {
		return ModelReader.read(data);
	}

	Geometry getGeometry();

	List<Material> getMaterials();

	Material getMaterial(int idx);

	List<TextureReference> getTextures();

	TextureReference getTextures(int idx);

	List<Bone> getBones();

	Bone getBone(int idx);

	int[] getBoneLookUp();

}