package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;

public class StructUInt8 implements VisitableStruct {

	@Order(1)
	@StructField(value = DataType.UBIT_8)
	public int value; //

	@Override
	public void visit(StructVisitor process, DataTracker fileReader, int dataPosition) {
	}

}
