package nexusvault.format.m3.v100;

public interface VisitableStruct {

	void visit(StructVisitor process, DataTracker fileReader, int dataPosition);

}
