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
		this.queue = new BitQueue();
	}

	@Override
	public void consume(int data, int numberOfBits) {
		this.queue.push(data, numberOfBits);
		flushBytes();
	}

	public void flush() {
		flushBytes();
		final var dataSize = this.queue.getAvailable();
		if (dataSize > 0) {
			var data = this.queue.pop(dataSize);
			data = data << Byte.SIZE - dataSize;
			this.output.writeInt8(data);
		}
	}

	private void flushBytes() {
		while (this.queue.getAvailable() >= Byte.SIZE) {
			final var data = this.queue.pop(Byte.SIZE);
			this.output.writeInt8(data);
		}
	}

	@Override
	public String toString() {
		return "[BitConsumer: Queue: " + this.queue + " + BinaryWriter: " + this.output + "]";
	}

}
