package nexusvault.format.tex.jpg.tool.decoder;

import java.nio.ByteBuffer;

import nexusvault.format.tex.jpg.tool.BitQueue;

public final class ByteBufferBitSupplier implements BitSupply {

	private final ByteBuffer input;
	private final BitQueue queue;

	public ByteBufferBitSupplier(ByteBuffer input) {
		if (input == null) {
			throw new IllegalArgumentException("'input' must not be null");
		}
		this.input = input;
		this.queue = new BitQueue();
	}

	public long getRemainingBits() {
		return this.queue.getAvailable() + this.input.remaining() * Byte.SIZE;
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
		if (this.queue.getAvailable() < nRequestedBits) {
			int diff = nRequestedBits - this.queue.getAvailable();
			if (diff > this.queue.getSpace()) {
				throw new IndexOutOfBoundsException();
			}
			while (diff > 0) {
				this.queue.push(this.input.get() & 0xFF, Byte.SIZE);
				diff -= Byte.SIZE;
			}
		}
		final int request = this.queue.pop(nRequestedBits);
		return request;
	}

	@Override
	public String toString() {
		return "[BitSupply: Remaining bits=" + getRemainingBits() + " Queue: " + this.queue + "]";
	}

}