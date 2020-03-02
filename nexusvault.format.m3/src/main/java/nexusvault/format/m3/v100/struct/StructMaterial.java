package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.BytePositionTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;
import nexusvault.format.m3.v100.pointer.ATP_MaterialDescriptor;

/**
 * Contains:
 * <ul>
 * <li>{@link StructMaterialDescriptor material descriptors}
 * </ul>
 * <p>
 * TODO
 */
public final class StructMaterial implements VisitableStruct {

	public static void main(String[] arg) {
		nexusvault.format.m3.v100.struct.SizeTest.ensureSizeAndOrder(StructMaterial.class, 0x30);
	}

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructMaterial.class);

	@Order(1)
	@StructField(value = DataType.BIT_8, length = 32)
	public int[] gap_000;

	@Order(2)
	@StructField(DataType.STRUCT)
	public ATP_MaterialDescriptor materialDescription;

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, materialDescription);
	}
}
