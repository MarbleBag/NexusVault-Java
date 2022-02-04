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
