package nexusvault.format.m3.export.gltf;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

final class VertexFieldBoneWeights extends VertexField {

	private int[] min;
	private int[] max;

	VertexFieldBoneWeights(int offsetWithinVertex) {
		super("BoneWeight", GlTFComponentType.FLOAT, GlTFType.VEC4, GlTFMeshAttribute.WEIGHTS, offsetWithinVertex);
		resetField();
	}

	@Override
	public void writeTo(BinaryWriter writer, ModelVertex vertex) {
		final int a = vertex.getBoneWeight1();
		final int b = vertex.getBoneWeight2();
		final int c = vertex.getBoneWeight3();
		final int d = vertex.getBoneWeight4();
		writer.writeFloat32(a / 255f);
		writer.writeFloat32(b / 255f);
		writer.writeFloat32(c / 255f);
		writer.writeFloat32(d / 255f);

		min[0] = Math.min(min[0], a & 0xFF);
		min[1] = Math.min(min[1], b & 0xFF);
		min[2] = Math.min(min[2], c & 0xFF);
		min[3] = Math.min(min[3], d & 0xFF);
		max[0] = Math.max(max[0], a & 0xFF);
		max[1] = Math.max(max[1], b & 0xFF);
		max[2] = Math.max(max[2], c & 0xFF);
		max[3] = Math.max(max[3], d & 0xFF);
	}

	@Override
	public void resetField() {
		super.resetField();

		min = new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
		max = new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };

		// min = new int[] { 0, 0, 0, 0 };
		// max = new int[] { 255, 255, 255, 255 };
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