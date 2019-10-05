package nexusvault.format.tex.struct;

import static kreed.reflection.struct.DataType.BIT_8;

import java.util.Objects;

import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;

public final class StructLayerInfo {

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructLayerInfo.class); // 0x6;

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
		super();
		this.quality = quality;
		this.hasReplacement = hasReplacement;
		this.replacement = replacement;
	}

	public boolean hasReplacement() {
		return hasReplacement != 0;
	}

	public byte getReplacement() {
		return replacement;
	}

	public byte getQuality() {
		return quality;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("LayerInfo [quality=");
		builder.append(quality);
		builder.append(", hasReplacement=");
		builder.append(hasReplacement);
		builder.append(", replacement=");
		builder.append(replacement);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(hasReplacement, quality, replacement);
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
		return (hasReplacement == other.hasReplacement) && (quality == other.quality) && (replacement == other.replacement);
	}

}