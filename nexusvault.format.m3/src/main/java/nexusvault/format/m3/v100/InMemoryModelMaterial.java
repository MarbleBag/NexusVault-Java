package nexusvault.format.m3.v100;

import java.util.List;

import nexusvault.format.m3.ModelMaterial;
import nexusvault.format.m3.ModelMaterialDescription;
import nexusvault.format.m3.v100.struct.StructMaterial;
import nexusvault.format.m3.v100.struct.StructMaterialDescriptor;

class InMemoryModelMaterial implements ModelMaterial {

	private final int idx;
	private final StructMaterial struct;
	private final InMemoryModel parent;

	public InMemoryModelMaterial(int idx, StructMaterial struct, InMemoryModel model) {
		this.idx = idx;
		this.struct = struct;
		this.parent = model;
	}

	public int getMaterialIndex() {
		return idx;
	}

	@Override
	public int getMaterialDescriptorCount() {
		return struct.materialDescription.getArraySize();
	}

	@Override
	public ModelMaterialDescription getMaterialDescription(int idx) {
		final StructMaterialDescriptor struct = parent.getStruct(this.struct.materialDescription, idx);
		final ModelMaterialDescription model = new InMemoryModelMaterialDescription(idx, struct, this);
		return model;
	}

	@Override
	public List<ModelMaterialDescription> getMaterialDescriptions() {
		return parent.getAllStructsPacked(struct.materialDescription, (idx, struct) -> new InMemoryModelMaterialDescription(idx, struct, this));
	}

}
