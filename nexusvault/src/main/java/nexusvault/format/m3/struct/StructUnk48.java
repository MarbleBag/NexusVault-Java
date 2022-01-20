package nexusvault.format.m3.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.format.m3.pointer.ATP_S2;
import nexusvault.shared.exception.StructException;

public final class StructUnk48 implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructUnk184.class);

	static {
		if (SIZE_IN_BYTES != 0x30) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(value = DataType.BIT_8, length = 32)
	public byte[] unk_value_000;

	@Order(2)
	@StructField(value = DataType.STRUCT)
	public ATP_S2 unk_offset_020; // 0x020

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
	}

}
