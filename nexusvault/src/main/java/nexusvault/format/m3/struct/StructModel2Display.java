package nexusvault.format.m3.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.shared.exception.StructException;

public final class StructModel2Display implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructModel2Display.class);

	static {
		if (SIZE_IN_BYTES != 0x04) {
			throw new StructException();
		}
	}

	/**
	 * Is referenced by the Creature2Display.tbl in column modelMeshId00 - modelMeshId15.
	 * <p>
	 * The index of this struct references {@link StructMesh#meshGroupId}
	 */
	@Order(1)
	@StructField(value = DataType.UBIT_16)
	public int modelMeshId; // 0x000

	/**
	 * Default to display. In case no further information about which mesh groups to render is given, render each mesh groups with a value of 1
	 * <ul>
	 * <li>0 - not default
	 * <li>1 - default
	 * </ul>
	 * Other values were not seen
	 */
	@Order(2)
	@StructField(value = DataType.UBIT_16)
	public int default2Render; // 0x002

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
	}

}
