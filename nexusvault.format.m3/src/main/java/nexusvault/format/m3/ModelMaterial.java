package nexusvault.format.m3;

import java.util.List;

public interface ModelMaterial {

	int getMaterialDescriptorCount();

	ModelMaterialDescription getMaterialDescription(int idx);

	List<ModelMaterialDescription> getMaterialDescriptions();

}
