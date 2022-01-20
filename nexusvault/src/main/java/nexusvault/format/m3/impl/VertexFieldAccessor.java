package nexusvault.format.m3.impl;

import java.nio.ByteBuffer;

import kreed.io.util.ByteBufferUtil;

public interface VertexFieldAccessor<T> {
	T current(ByteBuffer buffer);

	T next(ByteBuffer buffer);
}

abstract class AbstVertexFieldAccessor<T> implements VertexFieldAccessor<T> {

	private final int byteStride;

	protected AbstVertexFieldAccessor() {
		this(0);
	}

	protected AbstVertexFieldAccessor(int byteStride) {
		this.byteStride = byteStride;
	}

	@Override
	public T next(ByteBuffer buffer) {
		final int pos = buffer.position();
		final T r = current(buffer);
		buffer.position(pos + this.byteStride);
		return r;
	}

}

class VertexFieldLocationAccessorFloat extends AbstVertexFieldAccessor<float[]> {

	public VertexFieldLocationAccessorFloat(int byteStride) {
		super(byteStride);
	}

	@Override
	public float[] current(ByteBuffer buffer) {
		return new float[] { ByteBufferUtil.getFloat32(buffer), ByteBufferUtil.getFloat32(buffer), ByteBufferUtil.getFloat32(buffer) };
	}

}

class VertexFieldLocationAccessorInt extends AbstVertexFieldAccessor<float[]> {
	final private static float SCALE = 64f;

	public VertexFieldLocationAccessorInt(int byteStride) {
		super(byteStride);
	}

	@Override
	public float[] current(ByteBuffer buffer) {
		return new float[] { ByteBufferUtil.getInt16(buffer) / SCALE, ByteBufferUtil.getInt16(buffer) / SCALE, ByteBufferUtil.getInt16(buffer) / SCALE };
	}

}

class VertexFieldType3Accessor extends AbstVertexFieldAccessor<int[]> {

	public VertexFieldType3Accessor(int byteStride) {
		super(byteStride);
	}

	@Override
	public int[] current(ByteBuffer buffer) {
		return new int[] { ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer) };
	}

}

class VertexFieldType4Accessor extends AbstVertexFieldAccessor<int[]> {

	public VertexFieldType4Accessor(int byteStride) {
		super(byteStride);
	}

	@Override
	public int[] current(ByteBuffer buffer) {
		return new int[] { ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer) };
	}

}

class VertexFieldUVMapAccessor extends AbstVertexFieldAccessor<float[]> {

	public VertexFieldUVMapAccessor(int byteStride) {
		super(byteStride);
	}

	@Override
	public float[] current(ByteBuffer buffer) {
		return new float[] { ByteBufferUtil.getFloat16(buffer), ByteBufferUtil.getFloat16(buffer) };
	}

}

class VertexFieldType6Accessor extends AbstVertexFieldAccessor<int[]> {

	public VertexFieldType6Accessor(int byteStride) {
		super(byteStride);
	}

	@Override
	public int[] current(ByteBuffer buffer) {
		return new int[] { ByteBufferUtil.getUInt8(buffer) };
	}

}

interface VertexFieldSetter {
	void set(DefaultModelVertex vertex, ByteBuffer buffer);
}

class VertexFieldSetLocationFloat implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.xyz = new float[] { ByteBufferUtil.getFloat32(buffer), ByteBufferUtil.getFloat32(buffer), ByteBufferUtil.getFloat32(buffer) };
	}
}

class VertexFieldSetLocationInt implements VertexFieldSetter {
	final private static float SCALE = 1 << 10;

	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.xyz = new float[] { ByteBufferUtil.getInt16(buffer) / SCALE, ByteBufferUtil.getInt16(buffer) / SCALE, ByteBufferUtil.getInt16(buffer) / SCALE };
	}
}

class VertexFieldSetF3U1 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.f3_unk1 = new int[] { ByteBufferUtil.getInt8(buffer), ByteBufferUtil.getInt8(buffer) };
	}
}

class VertexFieldSetF3U2 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.f3_unk2 = new int[] { ByteBufferUtil.getInt8(buffer), ByteBufferUtil.getInt8(buffer) };
	}
}

class VertexFieldSetF3U3 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.f3_unk3 = new int[] { ByteBufferUtil.getInt8(buffer), ByteBufferUtil.getInt8(buffer) };
	}
}

class VertexFieldSetBoneIndex implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.boneIndex = new int[] { ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer),
				ByteBufferUtil.getUInt8(buffer) };
	}
}

class VertexFieldSetBoneWeight implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.boneWeight = new int[] { ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer), ByteBufferUtil.getUInt8(buffer),
				ByteBufferUtil.getUInt8(buffer) };
	}
}

class VertexFieldSetF4U3 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.f4_unk3 = new int[] { ByteBufferUtil.getInt8(buffer), ByteBufferUtil.getInt8(buffer), ByteBufferUtil.getInt8(buffer),
				ByteBufferUtil.getInt8(buffer) };
	}
}

class VertexFieldSetF4U4 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.f4_unk4 = new int[] { ByteBufferUtil.getInt8(buffer), ByteBufferUtil.getInt8(buffer), ByteBufferUtil.getInt8(buffer),
				ByteBufferUtil.getInt8(buffer) };
	}
}

class VertexFieldSetUVMap1 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.textureCoord = new float[] { ByteBufferUtil.getFloat16(buffer), ByteBufferUtil.getFloat16(buffer) };
	}
}

class VertexFieldSetUVMap2 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.textureCoord = new float[] { ByteBufferUtil.getFloat16(buffer), ByteBufferUtil.getFloat16(buffer), ByteBufferUtil.getFloat16(buffer),
				ByteBufferUtil.getFloat16(buffer) };
	}
}

class VertexFieldSetF6U1 implements VertexFieldSetter {
	@Override
	public void set(DefaultModelVertex vertex, ByteBuffer buffer) {
		vertex.f6_unk1 = ByteBufferUtil.getInt8(buffer);
	}
}
