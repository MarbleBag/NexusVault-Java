package nexusvault.format.m3.v100.pointer;

import nexusvault.format.m3.v100.BytePositionTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;

/**
 * Indicates that a pointer as no specific type
 */
public final class NoType implements VisitableStruct {

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
	}

}
