package nexusvault.format.m3.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.format.m3.pointer.DATP_S4_S1;
import nexusvault.format.m3.pointer.DATP_S4_S4;
import nexusvault.shared.exception.StructException;

public class StructUnk184 implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructUnk184.class);

	static {
		if (SIZE_IN_BYTES != 0xB8) {
			throw new StructException();
		}
	}

	@Order(1)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] unk_value_000;

	@Order(2)
	@StructField(value = DataType.STRUCT)
	public DATP_S4_S4 unk_offset_008; //

	@Order(3)
	@StructField(value = DataType.STRUCT)
	public DATP_S4_S4 unk_offset_020; //

	@Order(4)
	@StructField(value = DataType.STRUCT)
	public DATP_S4_S1 unk_offset_038; //

	@Order(5)
	@StructField(value = DataType.STRUCT)
	public DATP_S4_S1 unk_offset_050; //

	@Order(6)
	@StructField(value = DataType.STRUCT)
	public DATP_S4_S1 unk_offset_068; //

	@Order(7)
	@StructField(value = DataType.STRUCT)
	public DATP_S4_S1 unk_offset_080; //

	@Order(8)
	@StructField(value = DataType.STRUCT)
	public DATP_S4_S4 unk_offset_098; //

	@Order(9)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] unk_value_0B0;

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, this.unk_offset_008);
		process.process(fileReader, dataPosition, this.unk_offset_020);
		process.process(fileReader, dataPosition, this.unk_offset_038);
		process.process(fileReader, dataPosition, this.unk_offset_050);
		process.process(fileReader, dataPosition, this.unk_offset_068);
		process.process(fileReader, dataPosition, this.unk_offset_080);
		process.process(fileReader, dataPosition, this.unk_offset_098);
	}
}
