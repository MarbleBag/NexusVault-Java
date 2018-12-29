package nexusvault.format.m3.v100.struct;

import java.nio.charset.Charset;
import java.util.Arrays;

import kreed.io.util.ByteBufferUtil;
import kreed.reflection.struct.DataType;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.format.m3.v100.DataTracker;
import nexusvault.format.m3.v100.StructVisitor;
import nexusvault.format.m3.v100.VisitableStruct;
import nexusvault.format.m3.v100.pointer.ATP_S2;

public class StructTexture implements VisitableStruct {
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructTexture.class);

	// -1, -1 -> not set?
	// 1, 0 - > ?
	// 2, 0 -> ?
	// 5, 0 -> ?
	// 6, 0 -> ?
	@Order(1)
	@StructField(value = DataType.BIT_8, length = 2)
	public int[] unk_gap_000; // o: 0x000

	// 0 diffuse, 1 normal, 2 FX(?)
	@Order(2)
	@StructField(DataType.BIT_8)
	public byte textureType; // o: 0x002

	@Order(3)
	@StructField(DataType.BIT_8)
	public byte unk_003; // o: 0x003

	@Order(4)
	@StructField(DataType.BIT_32)
	public int unk_value_004;

	@Order(5)
	@StructField(value = DataType.BIT_8, length = 8)
	public int[] unk_gap_008;

	@Order(6)
	@StructField(DataType.STRUCT)
	public ATP_S2 textureName; // 0-terminated, may be longer than necessary because of padding.

	private String name;

	@Override
	public void visit(StructVisitor process, DataTracker fileReader, int dataPosition) {
		process.process(fileReader, dataPosition, textureName);
	}

	public void setName(DataTracker data) {
		data.setPosition(textureName.getOffset());
		final Charset charset = ByteBufferUtil.getUTF16CharSet(data.getData());
		final int numberOfBytes = (textureName.getArraySize() - 1) * 2; // 0 terminated
		this.name = ByteBufferUtil.getString(data.getData(), numberOfBytes, charset);
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructTexture [unk_gap_000=");
		builder.append(Arrays.toString(unk_gap_000));
		builder.append(", textureType=");
		builder.append(textureType);
		builder.append(", unk_003=");
		builder.append(unk_003);
		builder.append(", unk_value_004=");
		builder.append(unk_value_004);
		builder.append(", unk_gap_008=");
		builder.append(Arrays.toString(unk_gap_008));
		builder.append(", textureName=");
		builder.append(textureName);
		builder.append(", name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((textureName == null) ? 0 : textureName.hashCode());
		result = (prime * result) + textureType;
		result = (prime * result) + unk_003;
		result = (prime * result) + Arrays.hashCode(unk_gap_000);
		result = (prime * result) + Arrays.hashCode(unk_gap_008);
		result = (prime * result) + unk_value_004;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final StructTexture other = (StructTexture) obj;
		if (textureName == null) {
			if (other.textureName != null) {
				return false;
			}
		} else if (!textureName.equals(other.textureName)) {
			return false;
		}
		if (textureType != other.textureType) {
			return false;
		}
		if (unk_003 != other.unk_003) {
			return false;
		}
		if (!Arrays.equals(unk_gap_000, other.unk_gap_000)) {
			return false;
		}
		if (!Arrays.equals(unk_gap_008, other.unk_gap_008)) {
			return false;
		}
		if (unk_value_004 != other.unk_value_004) {
			return false;
		}
		return true;
	}

}
