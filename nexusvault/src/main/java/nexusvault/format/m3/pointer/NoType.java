package nexusvault.format.m3.pointer;

import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;

/**
 * Indicates that a pointer as no specific type
 */
public final class NoType implements VisitableStruct {

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
	}

}
