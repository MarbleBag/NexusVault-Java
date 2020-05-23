package nexusvault.format.m3.v100;

import nexusvault.format.m3.ModelBone;
import nexusvault.format.m3.v100.struct.StructBones;

/**
 * Internal implementation. May change without notice.
 */
final class InMemoryModelBone implements ModelBone {

	private final int idx;
	private final StructBones struct;
	private final InMemoryModel model;

	public InMemoryModelBone(int idx, StructBones struct, InMemoryModel model) {
		this.idx = idx;
		this.struct = struct;
		this.model = model;
	}

	@Override
	public float[] getTransformationMatrix() {
		return this.struct.matrix_0D0;
	}

	@Override
	public float[] getInverseTransformationMatrix() {
		return this.struct.matrix_110;
	}

	@Override
	public int getBoneIndex() {
		return this.idx;
	}

	@Override
	public float getLocationX() {
		return this.struct.x;
	}

	@Override
	public float getLocationY() {
		return this.struct.y;
	}

	@Override
	public float getLocationZ() {
		return this.struct.z;
	}

	@Override
	public boolean hasParentBone() {
		return this.struct.parentId != -1;
	}

	@Override
	public int getParentBoneReference() {
		return this.struct.parentId;
	}

}
