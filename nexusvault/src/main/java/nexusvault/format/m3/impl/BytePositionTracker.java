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

package nexusvault.format.m3.impl;

import java.nio.ByteBuffer;

public final class BytePositionTracker {
	private final int start;
	private final int end;
	private final ByteBuffer buffer;

	public BytePositionTracker(int start, int end, ByteBuffer buffer) {
		super();
		this.start = start;
		this.end = end;
		this.buffer = buffer;
	}

	public long getDataStart() {
		return this.start;
	}

	public long getDataEnd() {
		return this.end;
	}

	public ByteBuffer getData() {
		return this.buffer;
	}

	public long getPosition() {
		return this.buffer.position();
	}

	public void resetPosition() {
		this.buffer.position(this.start);
	}

	public void setPosition(long position) {
		final int newPosition = (int) (this.start + position);
		if (newPosition < this.start || this.end < newPosition) {
			throw new IndexOutOfBoundsException("[" + this.start + "; " + this.end + "] was " + newPosition);
		}
		this.buffer.position(newPosition);
	}

	public void move(int bytes) {
		setPosition(this.buffer.position() + bytes);
	}
}