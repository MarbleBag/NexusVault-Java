package nexusvault.format.m3.v100;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import kreed.reflection.struct.DataReadDelegator;
import kreed.reflection.struct.StructFactory;
import kreed.reflection.struct.StructReader;
import kreed.reflection.struct.reader.ByteBufferReader;
import nexusvault.format.m3.Model;
import nexusvault.format.m3.ModelBone;
import nexusvault.format.m3.ModelGeometry;
import nexusvault.format.m3.ModelMaterial;
import nexusvault.format.m3.ModelTexture;
import nexusvault.format.m3.v100.pointer.ArrayTypePointer;
import nexusvault.format.m3.v100.struct.StructBones;
import nexusvault.format.m3.v100.struct.StructGeometry;
import nexusvault.format.m3.v100.struct.StructM3Header;
import nexusvault.format.m3.v100.struct.StructMaterial;
import nexusvault.format.m3.v100.struct.StructTexture;

final class InMemoryModel implements Model {

	private final static StructReader<ByteBuffer> structBuilder = StructReader.build(StructFactory.build(), DataReadDelegator.build(new ByteBufferReader()),
			false);

	protected final StructM3Header header;
	protected final DataTracker modelData;

	public InMemoryModel(StructM3Header header, DataTracker data) {
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
		if ((idx < 0) || (pointer.getArraySize() <= idx)) {
			throw new IndexOutOfBoundsException();
		}
		final int position = (int) (pointer.getOffset() + (pointer.getElementSize() * idx));
		this.modelData.setPosition(position);
		return getStruct(pointer.getTypeOfElement());
	}

	protected <T extends VisitableStruct> List<T> getAllStructs(ArrayTypePointer<T> pointer) {
		this.modelData.setPosition(pointer.getOffset());
		final int count = pointer.getArraySize();
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

	protected DataTracker getMemory() {
		return modelData;
	}

	protected StructGeometry getStructGeometry() {
		return getStruct(header.geometry, 0);
	}

	@Override
	public List<ModelTexture> getTextures() {
		return getAllStructsPacked(header.textures, (idx, struct) -> new InMemoryModelTexture(struct, this));
	}

	@Override
	public ModelTexture getTextures(int idx) {
		final StructTexture struct = getStruct(header.textures, idx);
		final InMemoryModelTexture model = new InMemoryModelTexture(struct, this);
		return model;
	}

	@Override
	public ModelGeometry getGeometry() {
		return new InMemoryModelGeometry(this, getStructGeometry());
	}

	@Override
	public ModelMaterial getMaterial(int idx) {
		final StructMaterial struct = getStruct(header.material, idx);
		final InMemoryModelMaterial model = new InMemoryModelMaterial(idx, struct, this);
		return model;
	}

	@Override
	public List<ModelMaterial> getMaterials() {
		return getAllStructsPacked(header.material, (idx, struct) -> new InMemoryModelMaterial(idx, struct, this));
	}

	@Override
	public List<ModelBone> getBones() {
		return getAllStructsPacked(header.bones, (idx, struct) -> new InMemoryModelBone(idx, struct, this));
	}

	@Override
	public int[] getBoneLookUp() {
		this.modelData.setPosition(header.boneMapping.getOffset());
		final int[] lookUp = new int[header.boneMapping.getArraySize()];
		for (int i = 0; i < lookUp.length; ++i) {
			lookUp[i] = modelData.getData().getShort() & 0xFFFF;
		}
		return lookUp;
	}

	@Override
	public ModelBone getBone(int idx) {
		final StructBones struct = getStruct(header.bones, idx);
		final ModelBone model = new InMemoryModelBone(idx, struct, this);
		return model;
	}

}
