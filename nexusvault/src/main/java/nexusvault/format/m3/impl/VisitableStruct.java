package nexusvault.format.m3.impl;

public interface VisitableStruct {

	void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition);

}
