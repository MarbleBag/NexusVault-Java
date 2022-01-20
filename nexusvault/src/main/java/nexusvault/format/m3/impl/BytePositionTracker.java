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
		return start;
	}

	public long getDataEnd() {
		return end;
	}

	public ByteBuffer getData() {
		return buffer;
	}

	public long getPosition() {
		return buffer.position();
	}

	public void resetPosition() {
		buffer.position(start);
	}

	public void setPosition(long position) {
		final int newPosition = (int) (start + position);
		if ((newPosition < start) || (end < newPosition)) {
			throw new IndexOutOfBoundsException("[" + start + "; " + end + "] was " + newPosition);
		}
		buffer.position(newPosition);
	}

	public void move(int bytes) {
		setPosition(buffer.position() + bytes);
	}
}