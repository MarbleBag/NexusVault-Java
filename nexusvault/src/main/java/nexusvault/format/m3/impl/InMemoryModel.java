package nexusvault.format.m3.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import kreed.reflection.struct.reader.ByteBufferReader;
import nexusvault.format.m3.Bone;
import nexusvault.format.m3.Geometry;
import nexusvault.format.m3.Material;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.TextureReference;
import nexusvault.format.m3.pointer.ArrayTypePointer;
import nexusvault.format.m3.struct.StructGeometry;
import nexusvault.format.m3.struct.StructM3Header;
import nexusvault.format.m3.struct.StructMaterial;
import nexusvault.format.m3.struct.StructTexture;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryModel implements Model {

	private final static StructReader<ByteBuffer> structBuilder = StructReader.build(StructFactory.build(), DataReadDelegator.build(new ByteBufferReader()),
			false);

	protected final StructM3Header header;
	protected final BytePositionTracker modelData;

	public InMemoryModel(StructM3Header header, BytePositionTracker data) {
		this.header = header;
		this.modelData = data;
	}

	protected <T extends VisitableStruct> T getStruct(Class<T> element) {
		return structBuilder.read(element, this.modelData.getData());
	}

	protected <T extends VisitableStruct> T getStruct(ArrayTypePointer<T> pointer) {
		this.modelData.setPosition(pointer.getOffset());
		return getStruct(pointer.getTypeOfElement());
	}

	protected <T extends VisitableStruct> T getStruct(ArrayTypePointer<T> pointer, int idx) {
		if (idx < 0 || pointer.getArrayLength() <= idx) {
			throw new IndexOutOfBoundsException();
		}
		final int position = (int) (pointer.getOffset() + pointer.getSizeOfElement() * idx);
		this.modelData.setPosition(position);
		return getStruct(pointer.getTypeOfElement());
	}

	protected <T extends VisitableStruct> List<T> getAllStructs(ArrayTypePointer<T> pointer) {
		this.modelData.setPosition(pointer.getOffset());
		final int count = pointer.getArrayLength();
		final List<T> result = new ArrayList<>(count);
		for (int i = 0; i < count; ++i) {
			final T struct = getStruct(pointer.getTypeOfElement());
			result.add(struct);
		}
		return result;
	}

	protected <T extends VisitableStruct, R> List<R> getAllStructsPacked(ArrayTypePointer<T> pointer, BiFunction<Integer, T, R> packer) {
		final List<T> structs = getAllStructs(pointer);
		final List<R> result = new ArrayList<>(structs.size());
		int idx = 0;
		for (final T struct : structs) {
			result.add(packer.apply(idx++, struct));
		}
		return result;
	}

	protected BytePositionTracker getMemory() {
		return this.modelData;
	}

	protected StructGeometry getStructGeometry() {
		return getStruct(this.header.geometry, 0);
	}

	@Override
	public List<TextureReference> getTextures() {
		return getAllStructsPacked(this.header.textures, (idx, struct) -> new InMemoryTextureReference(struct, this));
	}

	@Override
	public TextureReference getTextures(int idx) {
		final StructTexture struct = getStruct(this.header.textures, idx);
		final InMemoryTextureReference model = new InMemoryTextureReference(struct, this);
		return model;
	}

	@Override
	public Geometry getGeometry() {
		return new InMemoryGeometry(this, getStructGeometry());
	}

	@Override
	public Material getMaterial(int idx) {
		final StructMaterial struct = getStruct(this.header.material, idx);
		final InMemoryMaterial model = new InMemoryMaterial(idx, struct, this);
		return model;
	}

	@Override
	public List<Material> getMaterials() {
		return getAllStructsPacked(this.header.material, (idx, struct) -> new InMemoryMaterial(idx, struct, this));
	}

	@Override
	public List<Bone> getBones() {
		return getAllStructsPacked(this.header.bones, (idx, struct) -> new InMemoryBone(idx, struct, this));
	}

	@Override
	public int[] getBoneLookUp() {
		this.modelData.setPosition(this.header.boneMapping.getOffset());
		final int[] lookUp = new int[this.header.boneMapping.getArrayLength()];
		for (int i = 0; i < lookUp.length; ++i) {
			lookUp[i] = this.modelData.getData().getShort() & 0xFFFF;
		}
		return lookUp;
	}

	@Override
	public Bone getBone(int idx) {
		final var struct = getStruct(this.header.bones, idx);
		final var model = new InMemoryBone(idx, struct, this);
		return model;
	}

}
