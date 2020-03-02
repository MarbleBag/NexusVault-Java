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
	public float[] getTransformationMatrix(int idx) {
		if (idx == 0) {
			return struct.matrix_0D0;
		} else {
			return struct.matrix_110;
		}
	}

	@Override
	public int getBoneIndex() {
		return idx;
	}

	@Override
	public float getLocationX() {
		return struct.x;
	}

	@Override
	public float getLocationY() {
		return struct.y;
	}

	@Override
	public float getLocationZ() {
		return struct.z;
	}

	@Override
	public boolean hasParentBone() {
		return struct.parentId != -1;
	}

	@Override
	public int getParentBoneReference() {
		return struct.parentId;
	}

}
