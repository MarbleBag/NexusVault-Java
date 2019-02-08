package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;
import nexusvault.format.m3.v100.pointer.ATP_Bones;
import nexusvault.format.m3.v100.pointer.ATP_Geometry;
import nexusvault.format.m3.v100.pointer.ATP_Material;
import nexusvault.format.m3.v100.pointer.ATP_Model2Display;
import nexusvault.format.m3.v100.pointer.ATP_S1;
import nexusvault.format.m3.v100.pointer.ATP_S104;
import nexusvault.format.m3.v100.pointer.ATP_S112;
import nexusvault.format.m3.v100.pointer.ATP_S152;
import nexusvault.format.m3.v100.pointer.ATP_S16;
import nexusvault.format.m3.v100.pointer.ATP_S160;
import nexusvault.format.m3.v100.pointer.ATP_S184;
import nexusvault.format.m3.v100.pointer.ATP_S2;
import nexusvault.format.m3.v100.pointer.ATP_S32;
import nexusvault.format.m3.v100.pointer.ATP_S4;
import nexusvault.format.m3.v100.pointer.ATP_S40;
import nexusvault.format.m3.v100.pointer.ATP_S400;
import nexusvault.format.m3.v100.pointer.ATP_S48;
import nexusvault.format.m3.v100.pointer.ATP_S56;
import nexusvault.format.m3.v100.pointer.ATP_S70;
import nexusvault.format.m3.v100.pointer.ATP_S76;
import nexusvault.format.m3.v100.pointer.ATP_S8;
import nexusvault.format.m3.v100.pointer.ATP_S80;
import nexusvault.format.m3.v100.pointer.ATP_Texture;
import nexusvault.format.m3.v100.pointer.ATP_UInt16;
import nexusvault.format.m3.v100.pointer.DATP_S4_S1;
import nexusvault.format.m3.v100.pointer.DATP_S4_S2;
import nexusvault.format.m3.v100.pointer.DATP_S4_S4;
import nexusvault.format.m3.v100.pointer.DATP_UInt32_UInt8;

public final class StructM3Header implements VisitableStruct {

	public static void main(String[] arg) {
		nexusvault.format.m3.v100.struct.SizeTest.ensureSizeAndOrder(StructM3Header.class, 0x630);
	}

	public static final int SIGNATURE = ('M' << 24) | ('O' << 16) | ('D' << 8) | 'L';
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructM3Header.class);

	@Order(1)
	@StructField(DataType.BIT_32)
	public int signature; // 0x000

	@Order(2)
	@StructField(DataType.BIT_32)
	public int version; // 0x004

	@Order(3)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] gap_008; // 0x008

	@Order(4)
	@StructField(DataType.STRUCT)
	public ATP_S112 unk_offset_010; // 0x010

	@Order(5)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_020; // 4, 1 // 0x020

	@Order(6)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_038; // 4, 1 // 0x038

	@Order(7)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_050; // 4, 1 // 0x050

	@Order(8)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_068; // 4, 1 // 0x068

	@Order(9)
	@StructField(DataType.STRUCT)
	public ATP_S48 unk_offset_080; // 48 // 0x080

	@Order(10)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_090; // 4, 1 // 0x090

	@Order(11)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_0A8; // 4, 1 // 0x0A8

	@Order(12)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_0C0; // 4, 1 // 0x0C0

	@Order(13)
	@StructField(DataType.STRUCT)
	public DATP_UInt32_UInt8 unk_offset_0D8; // 4, 1 // 0x0D8

	@Order(14)
	@StructField(DataType.STRUCT)
	public ATP_S184 unk_offset_0F0; // 184 // 0x0F0

	@Order(15)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_100; // 4, 4 // 0x100

	@Order(16)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_118; // 4, 4 // 0x118

	@Order(17)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_130; // 4, 1 // 0x130

	@Order(18)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_148; // 4, 1 // 0x148

	@Order(19)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_160; // 4, 4 // 0x160

	@Order(20)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] gap_178; // 0x178

	@Order(21)
	@StructField(DataType.STRUCT)
	public ATP_Bones bones; // o: 0x180

	@Order(22)
	@StructField(DataType.STRUCT)
	public ATP_S2 unk_offset_190; // 2 o: 0x190

	@Order(23)
	@StructField(DataType.STRUCT)
	public ATP_S2 unk_offset_1A0; // 2 o: 0x1A0

	@Order(24)
	@StructField(DataType.STRUCT)
	public ATP_UInt16 boneMapping; // 2 o: 0x1B0

	@Order(25)
	@StructField(DataType.STRUCT)
	public ATP_Texture textures; // o: 0x1C0

	@Order(26)
	@StructField(DataType.STRUCT)
	public ATP_S2 unk_offset_1D0; // 2 // could be material indices o: 0x1D0

	@Order(27)
	@StructField(DataType.STRUCT)
	public ATP_S152 unk_offset_1E0; // 152 o: 0x1E0

	@Order(28)
	@StructField(DataType.STRUCT)
	public ATP_Material material; // 48 o: 0x1F0

	@Order(29)
	@StructField(DataType.STRUCT)
	public ATP_Model2Display model2Display; // 4 o: 0x200

	/**
	 * Contains the mapping for model2display to meshGroupId. And it is meshGroupId := display2meshGroupId[model2display]
	 */
	@Order(30)
	@StructField(DataType.STRUCT)
	public ATP_UInt16 display2model; // 2 //0x210

	@Order(31)
	@StructField(DataType.STRUCT)
	public ATP_S70 unk_offset_220; // 70 o: 0x220

	@Order(32)
	@StructField(DataType.STRUCT)
	public ATP_S4 unk_offset_230; // 4 o: 0x230

	@Order(33)
	@StructField(DataType.STRUCT)
	public ATP_S112 unk_offset_240; // 112 o: 0x240

	@Order(34)
	@StructField(DataType.STRUCT)
	public ATP_Geometry geometry; // o: 0x250

	@Order(35)
	@StructField(DataType.STRUCT)
	public ATP_S4 unk_offset_260; // 4 Byte data o: 0x260

	@Order(36)
	@StructField(DataType.STRUCT)
	public ATP_UInt16 unk_offset_270; // 2 Byte data o: 0x270

	@Order(37)
	@StructField(DataType.STRUCT)
	public ATP_S8 unk_offset_280; // 8 Byte data o: 0x280

	@Order(38)
	@StructField(DataType.STRUCT)
	public DATP_S4_S2 unk_offset_290; // 4, 2 Byte data o: 0x290

	@Order(39)
	@StructField(DataType.STRUCT)
	public ATP_S16 unk_offset_2A8; // 16b -> M3H_2Tuple -> 2b //0x2A8

	@Order(40)
	@StructField(DataType.STRUCT)
	public ATP_S40 unk_offset_2B8; // 40b -> {30b,M3H_2Tuple -> 32b} //0x2B8

	@Order(41)
	@StructField(DataType.STRUCT)
	public ATP_S8 unk_offset_2C8; // 8b //0x2C8

	@Order(42)
	@StructField(DataType.STRUCT)
	public ATP_S2 unk_offset_2D8; // 2b //0x2D8

	@Order(43)
	@StructField(DataType.STRUCT)
	public ATP_S8 unk_offset_2E8; // 8b //0x2E8

	@Order(44)
	@StructField(DataType.STRUCT)
	public ATP_S160 unk_offset_2F8; // 160b //0x2F8

	@Order(45)
	@StructField(DataType.STRUCT)
	public ATP_S80 unk_offset_308; // 80b //0x308

	@Order(46)
	@StructField(DataType.STRUCT)
	public ATP_S400 unk_offset_318; // 400b //0x318

	@Order(47)
	@StructField(DataType.STRUCT)
	public ATP_S56 unk_offset_328; // 56b //0x328

	@Order(48)
	@StructField(DataType.STRUCT)
	public ATP_UInt16 unk_offset_338; // 2b //0x338

	@Order(49)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] gap_348; // 0x348

	@Order(50)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_350; // 4b, 4b //0x350

	@Order(51)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] gap_368; // 0x368

	@Order(52)
	@StructField(DataType.STRUCT)
	public DATP_S4_S4 unk_offset_370; // 4b, 4b //0x370

	@Order(53)
	@StructField(value = DataType.BIT_8, length = 264)
	public byte[] gap_388; // 0x388

	@Order(54)
	@StructField(DataType.STRUCT)
	public ATP_S1 unk_offset_490; // b?? //0x490

	@Order(55)
	@StructField(DataType.STRUCT)
	public ATP_S4 unk_offset_4A0; // 4b //0x4A0

	@Order(56)
	@StructField(value = DataType.BIT_8, length = 96)
	public byte[] gap_4B0; // 0x4B0

	@Order(57)
	@StructField(DataType.STRUCT)
	public ATP_S16 unk_offset_510; // 16 //0x510

	@Order(58)
	@StructField(DataType.STRUCT)
	public ATP_S4 unk_offset_520; // 4 //0x520

	@Order(59)
	@StructField(DataType.STRUCT)
	public ATP_S4 unk_offset_530; // 4 //0x530

	@Order(60)
	@StructField(DataType.STRUCT)
	public ATP_S104 unk_offset_540; // 104 //0x540

	@Order(61)
	@StructField(DataType.STRUCT)
	public ATP_S2 unk_offset_550; // 2 //0x550

	@Order(62)
	@StructField(DataType.STRUCT)
	public ATP_S160 unk_offset_560; // 160 //0x560

	@Order(63)
	@StructField(DataType.STRUCT)
	public ATP_S32 unk_offset_570; // 32 //0x570

	@Order(64)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] gap_580; // 0x580

	@Order(65)
	@StructField(DataType.STRUCT)
	public ATP_S32 unk_offset_588; // 32 //0x588

	@Order(66)
	@StructField(DataType.STRUCT)
	public ATP_S76 unk_offset_598; // 76 //0x598

	@Order(67)
	@StructField(DataType.STRUCT)
	public ATP_S2 unk_offset_5A8; // 2 //0x5A8

	@Order(68)
	@StructField(value = DataType.BIT_8, length = 8)
	public byte[] gap_5B8; // 0x5B8

	@Order(69)
	@StructField(DataType.STRUCT)
	public DATP_S4_S1 unk_offset_5C0; // 4, 1 //0x5C0

	@Order(70)
	@StructField(value = DataType.BIT_8, length = 88)
	public byte[] gap_5D8; // 0x5D8

	@Override
	public void visit(StructVisitor process, DataTracker fileReader, int dataPosition) {

		process.process(fileReader, dataPosition, unk_offset_010);
		process.process(fileReader, dataPosition, unk_offset_020);
		process.process(fileReader, dataPosition, unk_offset_038);
		process.process(fileReader, dataPosition, unk_offset_050);
		process.process(fileReader, dataPosition, unk_offset_068);
		process.process(fileReader, dataPosition, unk_offset_080);
		process.process(fileReader, dataPosition, unk_offset_090);
		process.process(fileReader, dataPosition, unk_offset_0A8);
		process.process(fileReader, dataPosition, unk_offset_0C0);
		process.process(fileReader, dataPosition, unk_offset_0D8);
		process.process(fileReader, dataPosition, unk_offset_0F0);
		process.process(fileReader, dataPosition, unk_offset_100);
		process.process(fileReader, dataPosition, unk_offset_118);
		process.process(fileReader, dataPosition, unk_offset_130);
		process.process(fileReader, dataPosition, unk_offset_148);
		process.process(fileReader, dataPosition, unk_offset_160);

		process.process(fileReader, dataPosition, bones);
		process.process(fileReader, dataPosition, unk_offset_190);
		process.process(fileReader, dataPosition, unk_offset_1A0);
		process.process(fileReader, dataPosition, boneMapping);
		process.process(fileReader, dataPosition, textures);
		process.process(fileReader, dataPosition, unk_offset_1D0);
		process.process(fileReader, dataPosition, unk_offset_1E0);
		process.process(fileReader, dataPosition, material);
		process.process(fileReader, dataPosition, model2Display);
		process.process(fileReader, dataPosition, display2model);
		process.process(fileReader, dataPosition, unk_offset_220);
		process.process(fileReader, dataPosition, unk_offset_230);
		process.process(fileReader, dataPosition, unk_offset_240);
		process.process(fileReader, dataPosition, geometry);
		process.process(fileReader, dataPosition, unk_offset_260);
		process.process(fileReader, dataPosition, unk_offset_270);
		process.process(fileReader, dataPosition, unk_offset_280);
		process.process(fileReader, dataPosition, unk_offset_290);
		process.process(fileReader, dataPosition, unk_offset_2A8);
		process.process(fileReader, dataPosition, unk_offset_2B8);
		process.process(fileReader, dataPosition, unk_offset_2C8);
		process.process(fileReader, dataPosition, unk_offset_2D8);
		process.process(fileReader, dataPosition, unk_offset_2E8);
		process.process(fileReader, dataPosition, unk_offset_2F8);
		process.process(fileReader, dataPosition, unk_offset_308);
		process.process(fileReader, dataPosition, unk_offset_318);
		process.process(fileReader, dataPosition, unk_offset_328);
		process.process(fileReader, dataPosition, unk_offset_338);

		process.process(fileReader, dataPosition, unk_offset_350);
		process.process(fileReader, dataPosition, unk_offset_370);

		process.process(fileReader, dataPosition, unk_offset_490);
		process.process(fileReader, dataPosition, unk_offset_4A0);

		process.process(fileReader, dataPosition, unk_offset_510);
		process.process(fileReader, dataPosition, unk_offset_520);
		process.process(fileReader, dataPosition, unk_offset_530);
		process.process(fileReader, dataPosition, unk_offset_540);
		process.process(fileReader, dataPosition, unk_offset_550);
		process.process(fileReader, dataPosition, unk_offset_560);
		process.process(fileReader, dataPosition, unk_offset_570);

		process.process(fileReader, dataPosition, unk_offset_588);
		process.process(fileReader, dataPosition, unk_offset_598);
		process.process(fileReader, dataPosition, unk_offset_5A8);

		process.process(fileReader, dataPosition, unk_offset_5C0);
	}

}
