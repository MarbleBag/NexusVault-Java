package nexusvault.format.m3;

import java.util.List;

public interface Material {

	int getMaterialDescriptorCount();

	MaterialDescription getMaterialDescription(int idx);

	List<MaterialDescription> getMaterialDescriptions();

}
