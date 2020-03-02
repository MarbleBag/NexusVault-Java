package nexusvault.format.m3;

import java.util.List;

public interface Model {

	ModelGeometry getGeometry();

	List<ModelMaterial> getMaterials();

	ModelMaterial getMaterial(int idx);

	List<ModelTexture> getTextures();

	ModelTexture getTextures(int idx);

	List<ModelBone> getBones();

	ModelBone getBone(int idx);

	int[] getBoneLookUp();

}
