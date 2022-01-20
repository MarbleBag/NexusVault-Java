package nexusvault.format.tex.jpg.tools.huffman;

public final class BitSupply implements HuffmanDecoder.BitSupply {
	private final BitQueue queue = new BitQueue();
	private final byte[] data;
	private int index;

	public BitSupply(byte[] data) {
		this.data = data;
	}

	public int remainingBits() {
		return this.queue.position() + (this.data.length - this.index) * 8 /* bits per byte */ ;
	}

	@Override
	public boolean canSupply(int requestedBits) {
		return requestedBits <= remainingBits();
	}

	@Override
	public int supply(int requestedBits) {
		if (requestedBits > 32) {
			throw new IndexOutOfBoundsException();
		}

		if (this.queue.position() < requestedBits) {
			var dif = requestedBits - this.queue.position();
			if (dif > this.queue.remainingCapacity()) {
				throw new IndexOutOfBoundsException();
			}

			while (dif > 0) {
				this.queue.push(this.data[this.index++] & 0xFF, 8);
				dif -= 8;
			}
		}
		return this.queue.pop(requestedBits);
	}

	public String ToString() {
		return "[BitSupply: Remaining bits=" + remainingBits() + " Queue: " + this.queue + "]";
	}
}
