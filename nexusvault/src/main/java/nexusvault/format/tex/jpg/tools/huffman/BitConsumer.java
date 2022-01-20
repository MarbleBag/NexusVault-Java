package nexusvault.format.tex.jpg.tools.huffman;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public final class BitConsumer implements HuffmanEncoder.BitConsumer {

	private final BitQueue queue = new BitQueue();
	private final ByteArrayOutputStream output;

	public BitConsumer() {
		this(new ByteArrayOutputStream());
	}

	public BitConsumer(ByteArrayOutputStream output) {
		this.output = Objects.requireNonNull(output);
	}

	@Override
	public void consume(int data, int numberOfBits) {
		this.queue.push(data, numberOfBits);
		flushBytes();
	}

	public long size() {
		return this.output.size();
	}

	public void flush() {
		flushBytes();
		final var dataSize = this.queue.position();
		if (dataSize > 0) {
			var data = this.queue.pop(dataSize);
			data = data << Byte.SIZE - dataSize | 0xFF >> dataSize;
			this.output.write(data & 0xFF);
		}
	}

	private void flushBytes() {
		while (this.queue.position() >= Byte.SIZE) {
			final var data = this.queue.pop(Byte.SIZE);
			this.output.write(data);
		}
	}

	public byte[] toByteArray() {
		return this.output.toByteArray();
	}

}
