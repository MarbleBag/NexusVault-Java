package nexusvault.format.m3.export.gltf;

import kreed.io.util.BinaryWriter;
import nexusvault.format.m3.ModelVertex;

/**
 * Abstraction to access various fields of a vertex.
 */
abstract class VertexField {
	private final GlTFComponentType componentType;
	private final GlTFType type;
	private final GlTFMeshAttribute attribute;
	private final String nameShort;
	private final int offsetWithinVertex;

	public VertexField(String nameShort, GlTFComponentType componentType, GlTFType type, GlTFMeshAttribute attribute, int offsetWithinVertex) {
		super();
		this.nameShort = nameShort;
		this.componentType = componentType;
		this.type = type;
		this.attribute = attribute;
		this.offsetWithinVertex = offsetWithinVertex;
	}

	public int getComponentType() {
		return componentType.getId();
	}

	public String getType() {
		return type.getId();
	}

	public String getAttributeKey() {
		return attribute.getAttributeKey();
	}

	public String getNameShort() {
		return nameShort;
	}

	public final int getSizeInBytes() {
		return componentType.getByteCount() * type.getComponentCount();
	}

	public final int getFieldOffset() {
		return offsetWithinVertex;
	}

	abstract public void writeTo(BinaryWriter writer, ModelVertex vertex);

	public void resetField() {
		// TODO Auto-generated method stub

	}

	public boolean hasMinimum() {
		return false;
	}

	public boolean hasMaximum() {
		return false;
	}

	public Number[] getMinimum() {
		return null;
	}

	public Number[] getMaximum() {
		return null;
	}

}