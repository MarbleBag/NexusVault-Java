package nexusvault.format.m3.impl;

import java.util.ArrayList;
import java.util.List;

import nexusvault.format.m3.Vertex;
import nexusvault.format.m3.VertexField;
import nexusvault.format.m3.struct.StructGeometry;

public final class ModelVertexBuilder {

	private final List<VertexFieldSetter> setter;

	public ModelVertexBuilder(StructGeometry geometry) {

		this.setter = new ArrayList<>(11);

		if (geometry.isVertexFieldAvailable(VertexField.LOCATION_A)) {
			this.setter.add(new VertexFieldSetLocationFloat());
		}

		if (geometry.isVertexFieldAvailable(VertexField.LOCATION_B)) {
			this.setter.add(new VertexFieldSetLocationInt());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_3_UNK_1)) {
			this.setter.add(new VertexFieldSetF3U1());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_3_UNK_2)) {
			this.setter.add(new VertexFieldSetF3U2());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_3_UNK_3)) {
			this.setter.add(new VertexFieldSetF3U3());
		}

		if (geometry.isVertexFieldAvailable(VertexField.BONE_MAP)) {
			this.setter.add(new VertexFieldSetBoneIndex());
		}

		if (geometry.isVertexFieldAvailable(VertexField.BONE_WEIGHTS)) {
			this.setter.add(new VertexFieldSetBoneWeight());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_4_UNK_1)) {
			this.setter.add(new VertexFieldSetF4U3());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_4_UNK_2)) {
			this.setter.add(new VertexFieldSetF4U4());
		}

		if (geometry.isVertexFieldAvailable(VertexField.UV_MAP_2)) {
			this.setter.add(new VertexFieldSetUVMap2());
		} else if (geometry.isVertexFieldAvailable(VertexField.UV_MAP_1)) {
			this.setter.add(new VertexFieldSetUVMap1());
		}

		if (geometry.isVertexFieldAvailable(VertexField.FIELD_6_UNK_1)) {
			this.setter.add(new VertexFieldSetF6U1());
		}

	}

	public Vertex read(BytePositionTracker memory) {
		final var vertex = new DefaultModelVertex();
		for (final VertexFieldSetter setter : this.setter) {
			setter.set(vertex, memory.getData());
		}
		return vertex;
	}

}
