package nexusvault.format.m3.v100.pointer;

import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;

final class NoType implements VisitableStruct {

	@Override
	public void visit(StructVisitor process, DataTracker fileReader, int dataPosition) {
	}

}
