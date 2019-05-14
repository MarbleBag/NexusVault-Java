package nexusvault.format.m3.v100;

import java.util.ArrayList;
import java.util.List;

import nexusvault.format.m3.ModelVertex;
import nexusvault.format.m3.v100.struct.StructGeometry;
import nexusvault.format.m3.v100.struct.StructGeometry.VertexField;

public final class VertexReader {

	private final List<VertexFieldSetter> setter;

	public VertexReader(StructGeometry geometry) {

		setter = new ArrayList<>(11);

		if (geometry.isVertexFieldAvailable(VertexField.LOCATION)) {
			switch (geometry.getVertexFieldLocationType()) {
				case FLOAT32:
					setter.add(new VertexFieldSetLocationFloat());
					break;
				case INT16:
					setter.add(new VertexFieldSetLocationInt());
					break;
			}
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_3_UNK_1)) {
			setter.add(new VertexFieldSetF3U1());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_3_UNK_2)) {
			setter.add(new VertexFieldSetF3U2());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_3_UNK_3)) {
			setter.add(new VertexFieldSetF3U3());
		}

		if (geometry.isVertexFieldAvailable(VertexField.BONE_MAP)) {
			setter.add(new VertexFieldSetBoneIndex());
		}

		if (geometry.isVertexFieldAvailable(VertexField.BONE_WEIGHTS)) {
			setter.add(new VertexFieldSetBoneWeight());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_4_UNK_1)) {
			setter.add(new VertexFieldSetF4U3());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_4_UNK_2)) {
			setter.add(new VertexFieldSetF4U4());
		}

		if (geometry.isVertexFieldAvailable(VertexField.UV_MAP_2)) {
			setter.add(new VertexFieldSetUVMap2());
		} else if (geometry.isVertexFieldAvailable(VertexField.UV_MAP_1)) {
			setter.add(new VertexFieldSetUVMap1());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_6_UNK_1)) {
			setter.add(new VertexFieldSetF6U1());
		}

	}

	public ModelVertex read(BytePositionTracker memory) {
		final DefaultModelVertex vertex = new DefaultModelVertex();
		for (final VertexFieldSetter setter : setter) {
			setter.set(vertex, memory.getData());
		}
		return vertex;
	}

}
