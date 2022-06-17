package nexusvault.format.m3.struct;

import static kreed.reflection.struct.DataType.BIT_16;
import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.BIT_8;
import static kreed.reflection.struct.DataType.STRUCT;
import static kreed.reflection.struct.DataType.UBIT_16;
import static kreed.reflection.struct.DataType.UBIT_32;
import static kreed.reflection.struct.DataType.UBIT_8;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.VertexField;
import nexusvault.format.m3.impl.BytePositionTracker;
import nexusvault.format.m3.impl.StructVisitor;
import nexusvault.format.m3.impl.VisitableStruct;
import nexusvault.format.m3.pointer.ATP_Mesh;
import nexusvault.format.m3.pointer.ATP_S1;
import nexusvault.format.m3.pointer.ATP_S4;
import nexusvault.format.m3.pointer.ATP_UInt16;
import nexusvault.format.m3.pointer.ATP_UInt32;
import nexusvault.shared.exception.StructException;

public final class StructGeometry implements VisitableStruct {

	public final static int SIZE_IN_BYTES = StructUtil.sizeOf(StructGeometry.class);

	static {
		if (SIZE_IN_BYTES != 0xC8) {
			throw new StructException("Invalid struct size");
		}
	}

	/**
	 * For all 34712 scanned m3, the first value is 80, the other seven are 0. This is probably a 64 uint, maybe used as a signature to identify this packet.
	 */
	@Order(1)
	@StructField(value = BIT_8, length = 8)
	public byte[] gap_000; // 0x000

	// For all 34712 scanned m3 empty (2018.12.11)
	@Order(2)
	@StructField(STRUCT)
	public ATP_S4 unk_offset_008; // 4b 0x008

	/** Total number of vertices */
	@Order(3)
	@StructField(UBIT_32)
	public long vertexBlockCount; // 0x018

	/** Size of a single vertex in bytes */
	@Order(4)
	@StructField(UBIT_16)
	public int vertexBlockSizeInBytes; // 0x01C

	/**
	 * This flag indicates which fields are used. The position of the flag (starting at 0) is also the array-index for lookup in any vertex related array.
	 *
	 * <li>vertexBlockFlags & 0x0001 != 0 -> vertexBlockFieldType[0] is used (value 1 or 2)</li>
	 * <li>vertexBlockFlags & 0x0002 != 0 -> vertexBlockFieldType[1] is used (value 3)</li>
	 * <li>vertexBlockFlags & 0x0004 != 0 -> vertexBlockFieldType[2] is used (value 3)</li>
	 * <li>vertexBlockFlags & 0x0008 != 0 -> vertexBlockFieldType[3] is used (value 3)</li>
	 * <li>vertexBlockFlags & 0x0010 != 0 -> vertexBlockFieldType[4] is used (value 4), 4 small numbers, ascending, 0 seems to indicate 'not used'. Bone
	 * indices</li>
	 * <li>vertexBlockFlags & 0x0020 != 0 -> vertexBlockFieldType[5] is used (value 4), 4 bytes, sums always up to 255. Bone weights</li>
	 * <li>vertexBlockFlags & 0x0040 != 0 -> vertexBlockFieldType[6] is used (value 4)</li>
	 * <li>vertexBlockFlags & 0x0080 != 0 -> vertexBlockFieldType[7] is used (value 4), 4 bytes, at any time, only one byte is set to -1, every other byte to
	 * 0</li>
	 * <li>vertexBlockFlags & 0x0100 != 0 -> vertexBlockFieldType[8] is used (value 5), uv map 1</li>
	 * <li>vertexBlockFlags & 0x0200 != 0 -> vertexBlockFieldType[9] is used (value 5, uv map 2</li>
	 * <li>vertexBlockFlags & 0x0400 != 0 -> vertexBlockFieldType[10] is used (value 6)</li>
	 *
	 */
	@Order(5)
	@StructField(UBIT_16)
	private int vertexBlockFlags; // 0x01E

	/**
	 * This array seems to indicate which field holds which information
	 *
	 * <ul>
	 * <li>0 - null
	 * <li>1 - Location (xyz), stored as 3 x 32bit float
	 * <li>2 - Location (xyz), stored as 3 x 16bit int
	 * <li>3 - 16bit, ??
	 * <li>4 - Bone map/weight (and some more bone stuff???). Upto 4 bones, stored as 4 x 8bit int
	 * <li>5 - UV map, stored as 2 x 16bit float
	 * <li>6 - 8bit, ??
	 * </ul>
	 * Other numbers not encountered
	 */
	@Order(6)
	@StructField(value = UBIT_8, length = 11)
	private byte[] vertexBlockFieldType; // 0x020

	/**
	 * Contains information from which byte to which byte certain informations can be found. Starting at i, a range ends at i+n with n>0 if [i+n] != 0
	 */
	@Order(7)
	@StructField(value = UBIT_8, length = 11)
	private byte[] vertexBlockFieldPosition; // 0x02B

	// Always 0 for all m3 (2018.12.11), prob. padding
	@Order(8)
	@StructField(UBIT_16)
	private int gap_036; // 0x036

	@Order(9)
	@StructField(STRUCT)
	public ATP_S1 vertexBlockData; // 1b // 0x038

	// For all 34712 scanned m3 empty (2018.12.11)
	@Order(10)
	@StructField(STRUCT)
	public ATP_S4 unk_offset_048; // 4b //0x048

	// For all 34712 scanned m3 empty (2018.12.11)
	@Order(11)
	@StructField(STRUCT)
	public ATP_S4 unk_offset_058; // 4b //0x058

	@Order(12)
	@StructField(UBIT_32)
	public long indexCount; // 0x068

	/**
	 * Seems relevant to {@link #vertexBlockCount}. As soon {@link #vertexBlockCount} passes 65536 (max value for uint16), this entry changes from {2,1} to
	 * {4,2}. <br>
	 * Opposite to {2,1}, {4,2} indices do not start at 0 for each submesh. The index can also be far greater than the number of available vertices. Additional,
	 * if 4 bytes are read, the bytes which hold the actual index can be the first 2 LSB or MSB.
	 */
	@Order(13)
	@StructField(value = UBIT_8, length = 2)
	public byte[] unk_offset_06C; // 0x06C

	@Order(14)
	@StructField(BIT_16)
	public int padding_06E; // 0x06E //padding

	@Order(15)
	@StructField(STRUCT)
	public ATP_S1 indexData; // 1b //0x070

	@Order(16)
	@StructField(STRUCT)
	public ATP_Mesh meshes; // 112b //0x080

	/**
	 * Mostly equal to {@link #vertexBlockCount}, probably part of a struct within this struct, so this value doesn't need to be passed around as a separate
	 * value. <br>
	 * Sometimes this value is smaller than the other
	 */
	@Order(17)
	@StructField(UBIT_32)
	public long nVertexBlocks2; // 0x090

	// For all 34712 scanned m3 empty (2018.12.11) Maybe padding
	@Order(18)
	@StructField(BIT_32)
	public int gap_093;

	/**
	 * This pointer directs to an array <b>A</b>, containing indices for the vertex block array. Start index and end index, for mesh <b>i</b> can be found at
	 * <b>A[i]</b> and <b>A[i+1]</b>
	 * <p>
	 * Prob. legacy stuff, because this information is also encoded in each mesh, or used as a lookup table so it's not necessary to follow the mesh pointer.
	 */
	@Order(19)
	@StructField(STRUCT)
	public ATP_UInt32 meshVertexBlockRange; // 4b //0x098

	/**
	 * Contains a huge amount of data, related to meshes, see unk_offset_0B8 <br>
	 * Number of elements seems to be identical to (numberOfIndices/3)-1. <br>
	 * Maybe related to faces? Could be normalized face-normals (2 x Byte nx,ny)
	 */
	@Order(20)
	@StructField(STRUCT)
	public ATP_UInt16 unk_offset_0A8; // 2b //0x0A8

	// Maybe related to mesh, seems to contain |mesh|+1 elements
	// Related to unk_offset_0A8, it seems it contains ranges like submeshVertexBlockRange
	// goes from 0 to X to unk_offset_0A8.size - 1

	@Order(21)
	@StructField(STRUCT)
	public ATP_UInt32 unk_offset_0B8; // 4b //0x0B8

	@Override
	public void visit(StructVisitor process, BytePositionTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, this.unk_offset_008);

		process.process(fileReader, dataPosition, this.vertexBlockData);
		process.process(fileReader, dataPosition, this.unk_offset_048);
		process.process(fileReader, dataPosition, this.unk_offset_058);

		process.process(fileReader, dataPosition, this.indexData);
		process.process(fileReader, dataPosition, this.meshes);

		process.process(fileReader, dataPosition, this.meshVertexBlockRange);
		process.process(fileReader, dataPosition, this.unk_offset_0A8);
		process.process(fileReader, dataPosition, this.unk_offset_0B8);

		if (this.gap_093 != 0) {
			throw new IllegalStateException("gap_093 contains value");
		}
	}

	/**
	 * @see VertexField
	 * @deprecated
	 */
	@Deprecated
	public static enum VertexFieldOld {
		/** Contains the location of the vertex. How this field is read depends on {@link StructGeometry#getVertexFieldLocationType()} */
		LOCATION(0, 3),

		/** 2 byte */
		FIELD_3_UNK_1(1, 2), // prob. tangents

		/** 2 byte */
		FIELD_3_UNK_2(2, 2), // prob. normals

		/** 2 byte */
		FIELD_3_UNK_3(3, 2), // prob. binormals

		/** 4 x int8 values. Each value represents a bone index. A value of 0, besides the first value, indicates the value is not used. */
		BONE_MAP(4, 4),

		/** 4 x int8 values. A value of 0 indicates the value is not used. All values sum up to 255 */
		BONE_WEIGHTS(5, 4),

		/** 4 byte */
		FIELD_4_UNK_1(6, 4),

		/** 4 byte */
		FIELD_4_UNK_2(7, 4),

		/** 2 x float16 values, uv */
		UV_MAP_1(8, 2),

		/** 2 x float16 values, uv */
		UV_MAP_2(9, 2),

		/** 1 byte */
		FIELD_6_UNK_1(10, 1);

		private final int index;
		private final int valuesPerField;

		private VertexFieldOld(int index, int valuesPerField) {
			this.index = index;
			this.valuesPerField = valuesPerField;
		}

		public int getValueCount() {
			return this.valuesPerField;
		}
	}

	/**
	 * @see VertexField
	 * @deprecated
	 */
	@Deprecated
	public static enum VertexFieldLocationType {
		/** 3 x float32 values, xyz */
		FLOAT32(1),
		/** 3 x int16 values, xyz */
		INT16(2);

		private final int type;

		private VertexFieldLocationType(int type) {
			this.type = type;
		}
	}

	public Set<VertexField> getAvailableVertexFields() {
		final var enumSet = EnumSet.noneOf(VertexField.class);
		for (final var field : VertexField.values()) {
			if (isVertexFieldAvailable(field)) {
				enumSet.add(field);
			}
		}
		return enumSet;
	}

	public boolean isVertexFieldAvailable(VertexField field) {
		if (field == null) {
			throw new IllegalArgumentException("'field' must not be null");
		}

		final var hasFlag = (this.vertexBlockFlags & 1 << field.getIndex()) != 0;
		final var typeMatch = field.getType() == -1 || this.vertexBlockFieldType[field.getIndex()] == field.getType();
		return hasFlag && typeMatch;
	}

	public int getVertexFieldPosition(VertexField field) {
		if (field == null) {
			throw new IllegalArgumentException("'field' must not be null");
		}
		return this.vertexBlockFieldPosition[field.getIndex()];
	}

	public int[] getVertexFieldPositions(Collection<VertexField> fields) {
		if (fields == null) {
			throw new IllegalArgumentException("'fields' must not be null");
		}

		final var result = new int[fields.size()];
		var resultidx = 0;

		for (final var field : fields) {
			if (!isVertexFieldAvailable(field)) {
				continue;
			}
			result[resultidx++] = this.vertexBlockFieldPosition[field.getIndex()];
		}

		return result;
	}

	@Deprecated
	public final boolean isVertexFieldAvailable(VertexFieldOld field) {
		if (field == null) {
			throw new IllegalArgumentException("'field' must not be null");
		}
		return (this.vertexBlockFlags & 1 << field.index) != 0;
	}

	@Deprecated
	public final VertexFieldLocationType getVertexFieldLocationType() {
		final int type = getVertexFieldValue(VertexFieldOld.LOCATION);
		if (type == VertexFieldLocationType.INT16.type) {
			return VertexFieldLocationType.INT16;
		} else if (type == VertexFieldLocationType.FLOAT32.type) {
			return VertexFieldLocationType.FLOAT32;
		} else {
			return null;
		}
	}

	@Deprecated
	private final int getVertexFieldValue(VertexFieldOld field) {
		return this.vertexBlockFieldType[field.index];
	}

	@Deprecated
	public final int[] getVertexFieldPosition(VertexFieldOld field) {
		if (!isVertexFieldAvailable(field)) {
			throw new VertexBlockFieldNotFoundException(String.format("Vertex field %s not available.", field.name()));
		}

		for (int i = field.index + 1; i < this.vertexBlockFieldPosition.length; ++i) {
			if (this.vertexBlockFieldPosition[i] != 0) {
				return new int[] { this.vertexBlockFieldPosition[field.index], this.vertexBlockFieldPosition[i] };
			}
		}
		return new int[] { this.vertexBlockFieldPosition[field.index], this.vertexBlockSizeInBytes };
	}

}
