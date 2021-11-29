package nexusvault.format.tex.struct;

import static kreed.reflection.struct.DataType.BIT_8;

import java.util.Objects;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.StructException;

public final class StructJpgChannel {

	static {
		if (StructUtil.sizeOf(StructJpgChannel.class) != 0x3) {
			throw new StructException();
		}
	}

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructJpgChannel.class); // 0x3;

	/** uint8, 0-100 */
	@Order(1)
	@StructField(BIT_8)
	private byte quality;

	/** uint8, 0-1 */
	@Order(2)
	@StructField(BIT_8)
	private byte hasDefaultColor;

	/** uint8, 0-255 */
	@Order(3)
	@StructField(BIT_8)
	private byte defaultColor;

	public StructJpgChannel() {

	}

	public StructJpgChannel(byte quality, byte hasDefaultColor, byte defaultColor) {
		this.quality = quality;
		this.hasDefaultColor = hasDefaultColor;
		this.defaultColor = defaultColor;
	}

	public StructJpgChannel(BinaryReader reader) {
		this.quality = reader.readInt8();
		this.hasDefaultColor = reader.readInt8();
		this.defaultColor = reader.readInt8();
	}

	public boolean hasDefaultColor() {
		return this.hasDefaultColor != 0;
	}

	public void hasDefaultColor(boolean value) {
		this.hasDefaultColor = (byte) (value ? 1 : 0);
	}

	public byte getDefaultColor() {
		return this.defaultColor;
	}

	public void setDefaultColor(byte value) {
		this.defaultColor = value;
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
		builder.append(this.hasDefaultColor);
		builder.append(", replacement=");
		builder.append(this.defaultColor);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.hasDefaultColor, this.quality, this.defaultColor);
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
		final StructJpgChannel other = (StructJpgChannel) obj;
		return this.hasDefaultColor == other.hasDefaultColor && this.quality == other.quality && this.defaultColor == other.defaultColor;
	}

}