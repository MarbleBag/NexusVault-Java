package nexusvault.format.tex.jpg.tool.decoder;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryUnderflowException;
import nexusvault.format.tex.jpg.tool.BitQueue;

public final class BinaryReaderBitSupplier implements BitSupply {

	private final BinaryReader input;
	private final BitQueue queue;
	private int limit;

	public BinaryReaderBitSupplier(BinaryReader input, int limit) {
		if (input == null) {
			throw new IllegalArgumentException("'input' must not be null");
		}
		this.input = input;
		this.limit = limit;
		this.queue = new BitQueue();
	}

	public long getMaximumNumberOfBits() {
		return this.queue.getAvailable() + this.limit * (long) Byte.SIZE;
	}

	@Override
	public boolean canSupply(int nRequestedBits) {
		int nMissingBits = nRequestedBits - this.queue.getAvailable();
		while (nMissingBits > 0 && !this.input.isEndOfData() && this.limit > 0) {
			try {
				this.queue.push(this.input.readInt8() & 0xFF, Byte.SIZE);
				nMissingBits -= Byte.SIZE;
				this.limit -= 1;
			} catch (final BinaryUnderflowException e) {
				return false;
			}
		}
		return nRequestedBits <= this.queue.getAvailable();
	}

	@Override
	public int supply(int nRequestedBits) {
		if (nRequestedBits > Integer.SIZE) {
			throw new IndexOutOfBoundsException();
		}

		if (this.queue.getAvailable() < nRequestedBits) {
			throw new IndexOutOfBoundsException();
		}

		return this.queue.pop(nRequestedBits);
	}

	@Override
	public String toString() {
		return "[BitSupply: Remaining bits=" + getMaximumNumberOfBits() + " Queue: " + this.queue + " + BinaryReader: " + this.input + "]";
	}

}