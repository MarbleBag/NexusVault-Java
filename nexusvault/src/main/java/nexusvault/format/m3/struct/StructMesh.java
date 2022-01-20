package nexusvault.format.m3.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.MeshToBodyPart;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.shared.exception.StructException;

public final class StructMesh implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructMesh.class);

	static {
		if (SIZE_IN_BYTES != 0x70) {
			throw new StructException();
		}
	}

	/**
	 * This value is an index and defines at which position of {@link StructGeometry#indexData} the indices start, which define this mesh. The number of indices
	 * which belong to this mesh is given by {@link #indexCount}
	 */
	@Order(1)
	@StructField(value = DataType.UBIT_32)
	public long startIndex; // 0x000

	/**
	 * This value is an index and defines at which position of {@link StructGeometry#vertexBlockData} the vertices start, which define this mesh. The number of
	 * vertices which belong to this mesh is given by {@link #vertexCount}
	 * <p>
	 * The index is defined on a per vertex basis, the number of bytes which belongs to a vertex is defined by {@link StructGeometry#vertexBlockSizeInBytes}
	 */
	@Order(2)
	@StructField(value = DataType.UBIT_32)
	public long startVertex; // 0x004

	@Order(3)
	@StructField(value = DataType.UBIT_32)
	public long indexCount; // 0x008

	@Order(4)
	@StructField(value = DataType.UBIT_32)
	public long vertexCount; // 0x00C

	@Order(5)
	@StructField(value = DataType.BIT_8, length = 6)
	public byte[] gap_010; // 0x010

	/** {@link StructMaterial material} index, linked under {@link StructM3Header header} */
	@Order(6)
	@StructField(value = DataType.UBIT_8)
	public int materialSelector; // 0x016

	@Order(7)
	@StructField(value = DataType.BIT_8, length = 7)
	public byte[] gap_017; // 0x017

	/**
	 * Defines the group this mesh belongs to. <br>
	 * For models without groups, this seems to be -1
	 */
	@Order(8)
	@StructField(value = DataType.UBIT_8)
	public byte meshGroupId; // 0x1E

	/**
	 * Seems for most cases 0, if <code>meshGroupId</code> -1, then this is also -1.
	 */
	@Order(9)
	@StructField(value = DataType.UBIT_8)
	public byte unk_01F; // 0x1F

	@Order(10)
	@StructField(value = DataType.BIT_8, length = 2)
	public byte[] gap_020; // 0x20

	/**
	 *
	 * The <code>IDs</code> is assigned to meshes, which describe a specific area of a model, for example: left hand, right hand, neck, etc. <br>
	 * Seems only be used for playable race models. <br>
	 * It is reasonable to assume that this <code>ID</code> is used to blend out meshes if, for example, the model wears gear. The Ids can be found in the
	 * <b>ModelCluster.tbl</b>
	 *
	 * @see MeshToBodyPart
	 */
	@Order(12)
	@StructField(value = DataType.UBIT_8)
	public byte modelClusterId; // 0x22

	@Order(13)
	@StructField(value = DataType.BIT_8, length = 29)
	public byte[] gap_023; // 0x23

	@Order(14)
	@StructField(value = DataType.BIT_32, length = 4)
	public float[] gap_040; // 0x040

	@Order(15)
	@StructField(value = DataType.BIT_32, length = 4)
	public float[] gap_050; // 0x050

	@Order(16)
	@StructField(value = DataType.BIT_8, length = 16)
	public byte[] gap_060; // 0x060

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		// System.out.println("[" + (test++) + "] " + StructUtil.toString(this));
	}
}
