package nexusvault.format.m3.export.gltf;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

final class VertexFieldBoneIndices extends VertexField {

	private int[] min;
	private int[] max;

	VertexFieldBoneIndices(int offsetWithinVertex) {
		super("BoneIdx", GlTFComponentType.UINT16, GlTFType.VEC4, GlTFMeshAttribute.JOINTS, offsetWithinVertex);
		resetField();
	}

	@Override
	public void writeTo(BinaryWriter writer, ModelVertex vertex) {
		final int a = vertex.getBoneIndex1();
		final int b = vertex.getBoneIndex2();
		final int c = vertex.getBoneIndex3();
		final int d = vertex.getBoneIndex4();
		writer.writeInt16(a);
		writer.writeInt16(b);
		writer.writeInt16(c);
		writer.writeInt16(d);

		min[0] = Math.min(min[0], a & 0xFFFFF);
		min[1] = Math.min(min[1], b & 0xFFFFF);
		min[2] = Math.min(min[2], c & 0xFFFFF);
		min[3] = Math.min(min[3], d & 0xFFFFF);
		max[0] = Math.max(max[0], a & 0xFFFFF);
		max[1] = Math.max(max[1], b & 0xFFFFF);
		max[2] = Math.max(max[2], c & 0xFFFFF);
		max[3] = Math.max(max[3], d & 0xFFFFF);
	}

	@Override
	public void resetField() {
		super.resetField();

		min = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
		max = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };
	}

	@Override
	public boolean hasMinimum() {
		return false;
	}

	@Override
	public boolean hasMaximum() {
		return false;
	}

	@Override
	public Number[] getMinimum() {
		return new Number[] { min[0], min[1], min[2], min[3] };
	}

	@Override
	public Number[] getMaximum() {
		return new Number[] { max[0], max[1], max[2], max[3] };
	}

}