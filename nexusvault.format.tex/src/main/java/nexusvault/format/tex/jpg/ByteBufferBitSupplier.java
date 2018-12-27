package nexusvault.format.tex.jpg;

import java.nio.ByteBuffer;

class ByteBufferBitSupplier implements BitSupply {

	private final ByteBuffer input;
	private final BitQueue queue;

	public ByteBufferBitSupplier(ByteBuffer input) {
		if (input == null) {
			throw new IllegalArgumentException("'input' must not be null");
		}
		this.input = input;
		this.queue = new BitQueue();
	}

	public int getRemainingBits() {
		return queue.getAvailable() + (input.remaining() * Byte.SIZE);
	}

	@Override
	public boolean canSupply(int nRequestedBits) {
		return nRequestedBits <= getRemainingBits();
	}

	@Override
	public int supply(int nRequestedBits) {
		if (nRequestedBits > Integer.SIZE) {
			throw new IndexOutOfBoundsException();
		}
		if (queue.getAvailable() < nRequestedBits) {
			int diff = nRequestedBits - queue.getAvailable();
			if (diff > queue.getSpace()) {
				throw new IndexOutOfBoundsException();
			}
			while (diff > 0) {
				queue.push(input.get() & 0xFF, Byte.SIZE);
				diff -= Byte.SIZE;
			}
		}
		final int request = queue.pop(nRequestedBits);
		return request;
	}

	@Override
	public String toString() {
		return "[BitSupply:  Remaining bits=" + getRemainingBits() + " Queue: " + queue + "]";
	}

}