package nexusvault.format.m3.v100;

import nexusvault.format.m3.v100.pointer.ArrayTypePointer;
import nexusvault.format.m3.v100.pointer.DoubleArrayTypePointer;

public interface StructVisitor {

	void process(DataTracker fileReader, int dataPosition, ArrayTypePointer<?> pointer);

	void process(DataTracker fileReader, int dataPosition, DoubleArrayTypePointer<?, ?> pointer);

}
