package nexusvault.format.m3.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.shared.exception.StructException;

public final class StructUInt32 implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructUInt32.class);

	static {
		if (SIZE_IN_BYTES != 0x4) {
			throw new StructException();
		}
	}

	@Order(2)
	@StructField(value = DataType.UBIT_32)
	public long value; //

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
	}

}
