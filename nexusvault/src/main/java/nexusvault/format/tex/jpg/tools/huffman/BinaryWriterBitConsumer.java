/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.tex.jpg.tools.huffman;

import kreed.io.util.BinaryWriter;

public final class BinaryWriterBitConsumer implements HuffmanEncoder.BitConsumer {

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
		final var dataSize = this.queue.position();
		if (dataSize > 0) {
			var data = this.queue.pop(dataSize);
			data = data << Byte.SIZE - dataSize;
			this.output.writeInt8(data);
		}
	}

	private void flushBytes() {
		while (this.queue.position() >= Byte.SIZE) {
			final var data = this.queue.pop(Byte.SIZE);
			this.output.writeInt8(data);
		}
	}

	@Override
	public String toString() {
		return "[BitConsumer: Queue: " + this.queue + " + BinaryWriter: " + this.output + "]";
	}

}
