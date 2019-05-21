package nexusvault.format.m3.v100.struct;

import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.BytePositionTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;

public final class StructMesh implements VisitableStruct {

	public static void main(String[] arg) {
		nexusvault.format.m3.v100.struct.SizeTest.ensureSizeAndOrder(StructMesh.class, 0x70);
	}

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructMesh.class);

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

	// TODO
	private static enum MeshBinding {
		UNKNOWN(-1),
		NONE(0),
		LEFT_ANKLE(1),
		RIGHT_ANKLE(2),
		LEFT_UPPER_ARM(3),
		RIGHT_UPPER_ARM4(4),
		LEFT_LOWER_LEG(5),
		RIGHT_LOWER_LEG(6),
		LEFT_KNEE_BELOW(7),
		RIGHT_KNEE_BELOW(8),
		LOWER_CHEST_AND_BACK(9),
		SHOULDERS_AND_BACK(10),
		LEFT_EAR(11),
		RIGHT_EAR(12),
		LEFT_ELBOW(13),
		RIGHT_ELBOW(14),
		LEFT_FINGERS(16),
		RIGHT_FINGERS(17),
		LEFT_HEEL(18),
		RIGHT_HEEL(19),
		LEFT_LOWER_ARM(20),
		RIGHT_LOWER_ARM(21),
		LEFT_PALM(22),
		RIGHT_PALM(23),
		LEFT_KNEE(24),
		RIGHT_KNEE(25),
		NECK(26),
		PELVIC(27);

		private final int id;

		private MeshBinding(int id) {
			this.id = id;
		}

		public static MeshBinding resolve(int value) {
			for (final MeshBinding e : MeshBinding.values()) {
				if (e.id == value) {
					return e;
				}
			}
			return MeshBinding.UNKNOWN;
		}
	}

	/**
	 * Seems only be used for playable race models. <br>
	 * It seems that the used <code>IDs</code> are identical across all checked occurrences. <br>
	 * The <code>IDs</code> is assigned to meshes, which describe a specific area of a model, for example: left hand, right hand, neck, etc. <br>
	 * It is reasonable to assume that this <code>ID</code> is used to blend out meshes if, for example, the model wears gear. This still needs to be checked.
	 * The information may be part of a item model or may be found in a .tbl defining said item.
	 * <p>
	 * TODO: Find a better name for this value and define an enum to collect all values
	 * <ul>
	 * <li>1 - Left ankle
	 * <li>2 - right ankle
	 * <li>3 - Left upper arm
	 * <li>4 - Right upper arm
	 * <li>5 - Left lower leg
	 * <li>6 - Right lower leg
	 * <li>7 - Area below left knee
	 * <li>8 - Area below right knee
	 * <li>9 - Underbust and part of the back
	 * <li>10 - Both shoulders and area along back between them
	 * <li>11 - Left ear
	 * <li>12 - Right ear
	 * <li>13 - Left elbow
	 * <li>14 - Right elbow
	 * <li><b>15 - missing</b>
	 * <li>16 - Left fingers
	 * <li>17 - Right fingers
	 * <li>18 - Left heel
	 * <li>19 - Right heel
	 * <li>20 - Left lower arm
	 * <li>21 - Right lower arm
	 * <li>22 - Left palm
	 * <li>23 - Right palm
	 * <li>24 - Left knee
	 * <li>25 - Right knee
	 * <li>26 - Neck, lower head
	 * <li>27 - Pelvic
	 * <li>29 - Area of arm, right below left shoulder
	 * <li>30 - Area of arm, right below right shoulder
	 * <li><b>31 - missing</b>
	 * <li><b>32 - missing</b>
	 * <li>33 - Left thigh
	 * <li>34 - Right thigh
	 * <li>35 - Left toes
	 * <li>36 - Right toes
	 * <li>37 - Midriff
	 * <li>38 - Left wrist
	 * <li>39 - Right wrist
	 * <li><b>40 -&gt; 80 - missing</b>
	 * <li>81 - Head
	 * <li>85 - Chest
	 * <li>88 - Tail
	 * <li><b>89 - missing</b>
	 * <li><b>90 - missing</b>
	 * <li>91 - Upperbust (May be female specific)
	 * <li>92 - Neck start
	 * </ul>
	 */
	@Order(12)
	@StructField(value = DataType.UBIT_8)
	public byte meshAnatomyId; // 0x22

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
