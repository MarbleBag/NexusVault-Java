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

public final class BitQueue {
	private long bitQueue;
	private int pos;

	public int position() {
		return this.pos;
	}

	public int remainingCapacity() {
		return Long.SIZE - this.pos;
	}

	public int pop(int requestedBits) {
		if (requestedBits < 0 || Integer.SIZE < requestedBits) {
			throw new IllegalArgumentException("Can't pop less than 0 or more than 32 bits.");
		}
		if (this.pos < requestedBits) {
			throw new IllegalArgumentException(String.format("Queue contains %d bits. Unable to pop %d bits.", this.pos, requestedBits));
		}
		if (requestedBits == 0) {
			return 0;
		}

		final int result = (int) (this.bitQueue >>> Long.SIZE - requestedBits);
		this.bitQueue <<= requestedBits;
		this.pos -= requestedBits;
		return result;
	}

	public void push(int data, int lengthInBits) {
		if (lengthInBits < 0 || Integer.SIZE < lengthInBits) {
			throw new IllegalArgumentException("Can't push less than 0 or more than 32 bits.");
		}
		if (remainingCapacity() < lengthInBits) {
			throw new IllegalArgumentException(String.format("Queue can only store %d more bits. Unable to push %d bits.", remainingCapacity(), lengthInBits));
		}

		if (lengthInBits == 0) {
			return;
		}

		final int mask = 0xFFFFFFFF >>> Integer.SIZE - lengthInBits;
		final long cleanedData = data & mask;
		final long alignedData = cleanedData << remainingCapacity() - lengthInBits;
		this.bitQueue = this.bitQueue | alignedData;
		this.pos += lengthInBits;
	}

	public void clear() {
		this.pos = 0;
		this.bitQueue = 0;
	}

	@Override
	public String toString() {
		String bin = Long.toBinaryString(this.bitQueue).replaceAll(" ", "0");
		while (bin.length() < 64) {
			bin = "0" + bin;
		}
		if (this.pos == 0) {
			bin = "|" + bin;
		} else if (this.pos == Long.SIZE) {
			bin = bin + "|";
		} else {
			bin = bin.substring(0, this.pos) + "|" + bin.substring(this.pos);
		}
		return "[BitQueue: " + bin + "]";
	}
}
