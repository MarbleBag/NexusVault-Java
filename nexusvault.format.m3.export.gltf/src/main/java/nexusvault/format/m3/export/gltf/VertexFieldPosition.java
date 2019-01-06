package nexusvault.format.m3.export.gltf;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

final class VertexFieldPosition extends VertexField {

	private float[] min;
	private float[] max;

	VertexFieldPosition(int offsetWithinVertex) {
		super("Pos", GlTFComponentType.FLOAT, GlTFType.VEC3, GlTFMeshAttribute.POSITION, offsetWithinVertex);
		resetField();
	}

	@Override
	public void writeTo(BinaryWriter writer, ModelVertex vertex) {
		final float x = vertex.getLocationX();
		final float y = vertex.getLocationY();
		final float z = vertex.getLocationZ();
		writer.writeFloat32(x);
		writer.writeFloat32(y);
		writer.writeFloat32(z);

		min[0] = Math.min(min[0], x);
		min[1] = Math.min(min[1], y);
		min[2] = Math.min(min[2], z);
		max[0] = Math.max(max[0], x);
		max[1] = Math.max(max[1], y);
		max[2] = Math.max(max[2], z);
	}

	@Override
	public void resetField() {
		super.resetField();

		min = new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE };
		max = new float[] { -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };
	}

	@Override
	public boolean hasMinimum() {
		return true;
	}

	@Override
	public boolean hasMaximum() {
		return true;
	}

	@Override
	public Number[] getMinimum() {
		return new Number[] { min[0], min[1], min[2] };
	}

	@Override
	public Number[] getMaximum() {
		return new Number[] { max[0], max[1], max[2] };
	}
}