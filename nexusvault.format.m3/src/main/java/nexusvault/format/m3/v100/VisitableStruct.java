package nexusvault.format.m3.v100;

public interface VisitableStruct {

	void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition);

}
