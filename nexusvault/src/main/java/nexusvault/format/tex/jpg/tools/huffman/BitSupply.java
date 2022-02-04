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
