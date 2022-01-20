package nexusvault.format.m3.impl;

import java.util.List;

import nexusvault.format.m3.Material;
import nexusvault.format.m3.MaterialDescription;
import nexusvault.format.m3.struct.StructMaterial;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryMaterial implements Material {

	private final int idx;
	private final StructMaterial struct;
	private final InMemoryModel parent;

	public InMemoryMaterial(int idx, StructMaterial struct, InMemoryModel model) {
		this.idx = idx;
		this.struct = struct;
		this.parent = model;
	}

	public int getMaterialIndex() {
		return this.idx;
	}

	@Override
	public int getMaterialDescriptorCount() {
		return this.struct.materialDescription.getArrayLength();
	}

	@Override
	public MaterialDescription getMaterialDescription(int idx) {
		final var struct = this.parent.getStruct(this.struct.materialDescription, idx);
		final var model = new InMemoryMaterialDescription(idx, struct, this);
		return model;
	}

	@Override
	public List<MaterialDescription> getMaterialDescriptions() {
		return this.parent.getAllStructsPacked(this.struct.materialDescription, (idx, struct) -> new InMemoryMaterialDescription(idx, struct, this));
	}

}
