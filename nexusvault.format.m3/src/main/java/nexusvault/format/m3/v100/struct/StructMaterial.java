package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.DataTracker;
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
public class StructMaterial implements VisitableStruct {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructMaterial.class);

	public static void main(String[] arg) {
		System.out.println(StructUtil.analyzeStruct(StructMaterial.class, false));
	}

	@Order(1)
	@StructField(value = DataType.BIT_8, length = 32)
	public int[] gap_000;

	@Order(2)
	@StructField(DataType.STRUCT)
	public ATP_MaterialDescriptor materialDescription;

	@Override
	public void visit(StructVisitor process, DataTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, materialDescription);
	}
}
