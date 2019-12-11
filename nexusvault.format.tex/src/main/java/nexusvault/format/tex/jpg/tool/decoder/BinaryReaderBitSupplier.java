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
		queue = new BitQueue();
	}

	public int getMaximumNumberOfBits() {
		return queue.getAvailable() + (limit * Byte.SIZE);
	}

	@Override
	public boolean canSupply(int nRequestedBits) {
		int nMissingBits = nRequestedBits - queue.getAvailable();
		while ((nMissingBits > 0) && !input.isEndOfData() && (limit > 0)) {
			try {
				queue.push(input.readInt8() & 0xFF, Byte.SIZE);
				nMissingBits -= Byte.SIZE;
				limit -= 1;
			} catch (final BinaryUnderflowException e) {
				return false;
			}
		}
		return nRequestedBits <= queue.getAvailable();
	}

	@Override
	public int supply(int nRequestedBits) {
		if (nRequestedBits > Integer.SIZE) {
			throw new IndexOutOfBoundsException();
		}

		if (queue.getAvailable() < nRequestedBits) {
			throw new IndexOutOfBoundsException();
		}

		return queue.pop(nRequestedBits);
	}

	@Override
	public String toString() {
		return "[BitSupply: Remaining bits=" + getMaximumNumberOfBits() + " Queue: " + queue + " + BinaryReader: " + input + "]";
	}

}