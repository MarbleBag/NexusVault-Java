package nexusvault.format.m3.impl;

import nexusvault.format.m3.MaterialDescription;
import nexusvault.format.m3.struct.StructMaterialDescriptor;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryMaterialDescription implements MaterialDescription {

	private final int idx;
	private final StructMaterialDescriptor struct;
	private final InMemoryMaterial model;

	public InMemoryMaterialDescription(int idx, StructMaterialDescriptor struct, InMemoryMaterial model) {
		this.idx = idx;
		this.struct = struct;
		this.model = model;
	}

	@Override
	public int getTextureReferenceA() {
		return this.struct.textureSelectorA;
	}

	@Override
	public int getTextureReferenceB() {
		return this.struct.textureSelectorB;
	}

}
