package nexusvault.format.tex.jpg;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryUnderflowException;

final class BinaryReaderBitSupplier implements BitSupply {

	private final BinaryReader input;
	private final BitQueue queue;

	public BinaryReaderBitSupplier(BinaryReader input) {
		if (input == null) {
			throw new IllegalArgumentException("'input' must not be null");
		}
		this.input = input;
		this.queue = new BitQueue();
	}

	@Override
	public boolean canSupply(int nRequestedBits) {
		if (queue.getAvailable() > nRequestedBits) {
			return true;
		}

		while (!input.isEndOfData() && (nRequestedBits > 0)) {
			try {
				queue.push(input.readInt8() & 0xFF, Byte.SIZE);
				nRequestedBits -= Byte.SIZE;
			} catch (final BinaryUnderflowException e) {
				return false;
			}
		}

		return true;
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
		return "[BitSupply:  Queue: " + queue + " + BinaryReader: " + input + "]";
	}

}