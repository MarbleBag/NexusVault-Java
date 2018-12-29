package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;

public class StructSubMesh implements VisitableStruct {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructSubMesh.class);

	public static void main(String[] arg) {
		System.out.println(StructUtil.analyzeStruct(StructSubMesh.class, false));
		final int size = StructUtil.sizeOf(StructSubMesh.class);
		if (size != 0x70) {
			throw new IllegalStateException();
		}
	}

	@Order(1)
	@StructField(value = DataType.UBIT_32)
	public long startIndex; // 0x000

	@Order(2)
	@StructField(value = DataType.UBIT_32)
	public long startVertexBlock; // 0x004

	@Order(3)
	@StructField(value = DataType.UBIT_32)
	public long numberOfIndices; // 0x008

	@Order(4)
	@StructField(value = DataType.UBIT_32)
	public long numberOfVertices; // 0x00C

	@Order(5)
	@StructField(value = DataType.BIT_8, length = 6)
	public byte[] gap_010; // 0x010

	@Order(6)
	@StructField(value = DataType.UBIT_8)
	public int materialSelector; // 0x016

	@Order(7)
	@StructField(value = DataType.BIT_8, length = 41)
	public byte[] gap_017; // 0x017

	@Order(8)
	@StructField(value = DataType.BIT_32, length = 4)
	public float[] gap_040; // 0x017

	@Order(9)
	@StructField(value = DataType.BIT_32, length = 4)
	public float[] gap_050; // 0x017

	@Order(10)
	@StructField(value = DataType.BIT_8, length = 16)
	public byte[] gap_060; // 0x017

	@Override
	public void visit(StructVisitor process, DataTracker fileReader, int dataPosition) {
		// System.out.println("[" + (test++) + "] " + StructUtil.toString(this));
	}
}
