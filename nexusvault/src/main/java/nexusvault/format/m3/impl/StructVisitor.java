package nexusvault.format.m3.impl;

import nexusvault.format.m3.pointer.ArrayTypePointer;
import nexusvault.format.m3.pointer.DoubleArrayTypePointer;

public interface StructVisitor {

	void process(BytePositionTracker fileReader, int dataPosition, ArrayTypePointer<?> pointer);

	void process(BytePositionTracker fileReader, int dataPosition, DoubleArrayTypePointer<?, ?> pointer);

}
