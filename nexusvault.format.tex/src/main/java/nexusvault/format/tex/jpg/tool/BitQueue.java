package nexusvault.format.tex.jpg.tool;

public final class BitQueue {
	private long bitQueue;
	private int pos;

	public int getAvailable() {
		return pos;
	}

	public int getSpace() {
		return Long.SIZE - pos;
	}

	public int pop(int nBits) {
		if ((nBits < 0) || (pos < nBits) || (Integer.SIZE < nBits)) {
			throw new IndexOutOfBoundsException(String.format("Queue contains %d bits. Unable to pop %d bits.", pos, nBits));
		}
		if (nBits == 0) {
			return 0;
		}

		final int result = (int) (bitQueue >>> (Long.SIZE - nBits));
		bitQueue <<= nBits;
		pos -= nBits;
		return result;
	}

	public void push(int data, int lengthInBits) {
		if ((getSpace() < lengthInBits) || (lengthInBits < 0) || (Integer.SIZE < lengthInBits)) {
			throw new IndexOutOfBoundsException(String.format("Queue can only store %d more bits. Unable to push %d bits.", getSpace(), lengthInBits));
		}

		if (lengthInBits == 0) {
			return;
		}

		final int mask = 0xFFFFFFFF >>> (Integer.SIZE - lengthInBits);
		final long cleanedData = data & mask;
		final long alignedData = cleanedData << (getSpace() - lengthInBits);
		bitQueue = bitQueue | alignedData;
		pos += lengthInBits;
	}

	public void clear() {
		pos = 0;
		bitQueue = 0;
	}

	@Override
	public String toString() {
		String bin = Long.toBinaryString(bitQueue).replaceAll(" ", "0");
		while (bin.length() < 64) {
			bin = "0" + bin;
		}
		if (pos == 0) {
			bin = "|" + bin;
		} else if (pos == Long.SIZE) {
			bin = bin + "|";
		} else {
			bin = bin.substring(0, pos) + "|" + bin.substring(pos);
		}
		return "[BitQueue: " + bin + "]";
	}
}
