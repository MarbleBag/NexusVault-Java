package nexusvault.format.m3.v100.struct;

import static kreed.reflection.struct.DataType.BIT_16;
import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.BIT_8;
import static kreed.reflection.struct.DataType.STRUCT;

import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;
import nexusvault.format.m3.v100.pointer.DATP_S4_S12;
import nexusvault.format.m3.v100.pointer.DATP_S4_S6;
import nexusvault.format.m3.v100.pointer.DATP_S4_S8;

public class StructBones implements VisitableStruct {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructBones.class);

	public static class StructInnerBones {
		public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructInnerBones.class);

		@Order(1)
		@StructField(value = BIT_8, length = 12)
		public byte[] gap_000; // 0x0D0

		/**
		 * Close to always 0, for 9 models this is not 0
		 */
		@Order(2)
		@StructField(value = BIT_8, length = 4)
		public byte[] gap_00C; // 0x0DC
	}

	public static void main(String[] arg) {
		System.out.println(StructUtil.analyzeStruct(StructBones.class, false));
		if (0x160 != StructUtil.sizeOf(StructBones.class)) {
			throw new IllegalStateException();
		}
	}

	// Is sometimes not -1
	@Order(1)
	@StructField(BIT_32)
	public int gap_000;

	@Order(2)
	@StructField(BIT_16)
	public int parentId; // 0x004

	@Order(3)
	@StructField(value = BIT_8, length = 2)
	public byte[] gap_006; // 0x006

	@Order(4)
	@StructField(value = BIT_8, length = 4)
	public byte[] gap_008; // 0x008

	@Order(5)
	@StructField(BIT_32)
	public int padding_00C; // 0x00C

	// unk_block_010[2] is related to parent.unk_block_010[0]. Both pointer have (mostly) always the same amount of elements
	// Those values seem to be 0, for leaf bones
	@Order(6)
	@StructField(value = STRUCT, length = 4)
	public DATP_S4_S6[] unk_block_010;

	@Order(7)
	@StructField(value = STRUCT, length = 2)
	public DATP_S4_S8[] bone_animation; // 0x070

	@Order(8)
	@StructField(value = STRUCT, length = 2)
	public DATP_S4_S12[] unk_block_0A0; // 0x0A0

	@Order(9)
	@StructField(STRUCT)
	public StructInnerBones gap_0D0; // 0x0D0

	@Order(10)
	@StructField(STRUCT)
	public StructInnerBones gap_0E0; // 0x0E0

	@Order(11)
	@StructField(STRUCT)
	public StructInnerBones gap_0F0; // 0x0F0

	@Order(12)
	@StructField(STRUCT)
	public StructInnerBones gap_100; // 0x100

	@Order(13)
	@StructField(STRUCT)
	public StructInnerBones gap_110; // 0x110

	@Order(14)
	@StructField(STRUCT)
	public StructInnerBones gap_120; // 0x120

	@Order(15)
	@StructField(STRUCT)
	public StructInnerBones gap_130; // 0x130

	@Order(16)
	@StructField(STRUCT)
	public StructInnerBones gap140; // 0x140

	@Order(17)
	@StructField(BIT_32)
	public float x; // 0x150

	@Order(18)
	@StructField(BIT_32)
	public float y; // 0x154

	@Order(19)
	@StructField(BIT_32)
	public float z; // 0x158

	@Order(20)
	@StructField(BIT_32)
	public int padding_15C; // 0x15C

	@Override
	public void visit(StructVisitor process, DataTracker fileReader, int dataPosition) {
		for (final DATP_S4_S6 p : unk_block_010) {
			process.process(fileReader, dataPosition, p);
		}

		for (final DATP_S4_S8 p : bone_animation) {
			process.process(fileReader, dataPosition, p);
		}

		for (final DATP_S4_S12 p : unk_block_0A0) {
			process.process(fileReader, dataPosition, p);
		}

		if (padding_15C != 0) {
			throw new IllegalStateException("padding_15C contains value");
		}
	}

}
