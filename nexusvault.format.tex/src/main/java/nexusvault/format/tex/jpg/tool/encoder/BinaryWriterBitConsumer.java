package nexusvault.format.tex.jpg.tool.encoder;

import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.jpg.tool.BitQueue;

public final class BinaryWriterBitConsumer implements BitConsumer {

	private final BinaryWriter output;
	private final BitQueue queue;

	public BinaryWriterBitConsumer(BinaryWriter output) {
		if (output == null) {
			throw new IllegalArgumentException("'input' must not be null");
		}
		this.output = output;
		queue = new BitQueue();
	}

	@Override
	public void consume(int data, int numberOfBits) {
		queue.push(data, numberOfBits);
		flushBytes();
	}

	@Override
	public void endOfData() {
		flushBytes();
		final var dataSize = queue.getAvailable();
		if (dataSize > 0) {
			var data = queue.pop(dataSize);
			data = data << (Byte.SIZE - dataSize);
			output.writeInt8(data);
		}
	}

	private void flushBytes() {
		while (queue.getAvailable() > Byte.SIZE) {
			final var data = queue.pop(Byte.SIZE);
			output.writeInt8(data);
		}
	}

	@Override
	public String toString() {
		return "[BitConsumer: Queue: " + queue + " + BinaryWriter: " + output + "]";
	}

}
