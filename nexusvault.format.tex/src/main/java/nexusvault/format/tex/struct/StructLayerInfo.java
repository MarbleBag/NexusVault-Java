package nexusvault.format.tex.struct;

import static kreed.reflection.struct.DataType.BIT_8;

import java.util.Objects;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructLayerInfo {

	static {
		if (StructUtil.sizeOf(StructLayerInfo.class) != 0x3) {
			throw new StructException();
		}
	}

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructLayerInfo.class); // 0x3;

	/** uint8, 0-100 */
	@Order(1)
	@StructField(BIT_8)
	private byte quality;

	/** uint8, 0-1 */
	@Order(2)
	@StructField(BIT_8)
	private byte hasReplacement;

	/** uint8, 0-255 */
	@Order(3)
	@StructField(BIT_8)
	private byte replacement;

	public StructLayerInfo() {

	}

	public StructLayerInfo(byte quality, byte hasReplacement, byte replacement) {
		this.quality = quality;
		this.hasReplacement = hasReplacement;
		this.replacement = replacement;
	}

	public StructLayerInfo(BinaryReader reader) {
		this.quality = reader.readInt8();
		this.hasReplacement = reader.readInt8();
		this.replacement = reader.readInt8();
	}

	public boolean hasReplacement() {
		return this.hasReplacement != 0;
	}

	public void hasReplacement(boolean value) {
		this.hasReplacement = (byte) (value ? 1 : 0);
	}

	public byte getReplacement() {
		return this.replacement;
	}

	public void setReplacement(byte value) {
		this.replacement = value;
	}

	public byte getQuality() {
		return this.quality;
	}

	public void setQuality(int value) {
		setQuality((byte) value);
	}

	public void setQuality(byte value) {
		if (value < 0 || 100 < value) {
			throw new IllegalArgumentException();
		}
		this.quality = value;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("LayerInfo [quality=");
		builder.append(this.quality);
		builder.append(", hasReplacement=");
		builder.append(this.hasReplacement);
		builder.append(", replacement=");
		builder.append(this.replacement);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.hasReplacement, this.quality, this.replacement);
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
		final StructLayerInfo other = (StructLayerInfo) obj;
		return this.hasReplacement == other.hasReplacement && this.quality == other.quality && this.replacement == other.replacement;
	}

}