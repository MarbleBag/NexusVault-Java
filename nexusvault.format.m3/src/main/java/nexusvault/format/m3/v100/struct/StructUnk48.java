package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.BytePositionTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;
import nexusvault.format.m3.v100.pointer.ATP_S2;

public final class StructUnk48 implements VisitableStruct {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructUnk48.class);

	public static void main(String[] args) {
		SizeTest.ensureSizeAndOrder(StructUnk48.class, 0x30);
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
