package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class TextureRawDataByteArray implements TextureRawData {

	private final byte[] data;
	private final ByteOrder order;

	public TextureRawDataByteArray(byte[] data, ByteOrder order) {
		if ((data == null) || (order == null)) {
			throw new IllegalArgumentException();
		}
		this.data = data;
		this.order = order;
	}

	@Override
	public void copyTo(int srcPosition, byte[] dst, int dstPosition, int length) {
		System.arraycopy(this.data, srcPosition, dst, dstPosition, length);
	}

	@Override
	public ByteOrder getByteOrder() {
		return this.order;
	}

	@Override
	public ByteBuffer createView(int offset, int length) {
		if ((offset < 0) || (data.length <= offset)) {
			throw new IndexOutOfBoundsException(String.format("Offset out of bounds. Range [0;%d). Got %d", data.length, offset));
		}
		if ((length < 0) || (data.length < length)) {
			throw new IndexOutOfBoundsException(String.format("Length out of bounds. Range [0;%d]. Got %d", data.length, length));
		}
		if (data.length < (offset + length)) {
			throw new IndexOutOfBoundsException(String.format("Offset + length out of bounds. Range [0;%d]. Got %d", data.length, offset + length));
		}
		return ByteBuffer.wrap(data, offset, length).order(order);
	}

	@Override
	public int getSizeOfDataInBytes() {
		return this.data.length;
	}

}
