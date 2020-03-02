package nexusvault.format.m3.v100;

import nexusvault.format.m3.ModelMaterialDescription;
import nexusvault.format.m3.v100.struct.StructMaterialDescriptor;

/**
 * Internal implementation. May change without notice.
 */
final class InMemoryModelMaterialDescription implements ModelMaterialDescription {

	private final int idx;
	private final StructMaterialDescriptor struct;
	private final InMemoryModelMaterial model;

	public InMemoryModelMaterialDescription(int idx, StructMaterialDescriptor struct, InMemoryModelMaterial model) {
		this.idx = idx;
		this.struct = struct;
		this.model = model;
	}

	@Override
	public int getTextureReferenceA() {
		return struct.textureSelectorA;
	}

	@Override
	public int getTextureReferenceB() {
		return struct.textureSelectorB;
	}

}
