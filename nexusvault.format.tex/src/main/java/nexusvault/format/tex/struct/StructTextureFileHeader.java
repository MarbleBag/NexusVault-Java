package nexusvault.format.tex.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.STRUCT;

import java.util.Arrays;
import java.util.Objects;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.SignatureMismatchException;
import nexusvault.shared.exception.StructException;

public final class StructTextureFileHeader {

	static {
		if (StructUtil.sizeOf(StructTextureFileHeader.class) != 0x70) {
			throw new StructException();
		}
	}

	public static final String STR_SIGNATURE = "GFX";
	public static final int SIGNATURE = 'G' << 16 | 'F' << 8 | 'X';

	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructTextureFileHeader.class); // 0x70;

	@Order(1)
	@StructField(BIT_32)
	public int signature; // 0x00

	@Order(2)
	@StructField(BIT_32)
	public int version; // 0x04

	/** pixel, power of 2. Size of the largest mipmap. */
	@Order(3)
	@StructField(BIT_32)
	public int width; // 0x08

	/** pixel, power of 2. Size of the largest mipmap. */
	@Order(4)
	@StructField(BIT_32)
	public int height; // 0x0C

	@Order(5)
	@StructField(BIT_32)
	public int depth; // 0x10

	@Order(6)
	@StructField(BIT_32)
	public int sides; // 0x14

	/** number of stored mipmaps */
	@Order(7)
	@StructField(BIT_32)
	public int mipMaps; // 0x18

	/**
	 * <ul>
	 * <li>0 = uncompressed ( A8 R8 G8 B8 )
	 * <li>1 = uncompressed ( A8 R8 G8 B8 )
	 * <li>13=dxt1
	 * <li>14=dxt3
	 * <li>15=dxt5
	 * </ul>
	 */
	@Order(8)
	@StructField(BIT_32)
	public int format; // 0x1C

	@Order(9)
	@StructField(BIT_32)
	public boolean isJpg; // 0x20

	@Order(10)
	@StructField(BIT_32)
	public int jpgFormat; // 0x24

	@Order(11)
	@StructField(value = STRUCT, length = 4)
	public StructJpgChannel[] jpgChannelInfos; // 0x28

	@Order(12)
	@StructField(BIT_32)
	public int mipmapSizesCount; // 0x34

	@Order(13)
	@StructField(value = BIT_32, length = 13)
	public int[] mipmapSizes; // 0x38

	@Order(14)
	@StructField(BIT_32)
	public int unk_06C; // 0x6C

	public StructTextureFileHeader() {
	}

	public StructTextureFileHeader(boolean initialized) {
		if (!initialized) {
			return;
		}

		this.signature = StructTextureFileHeader.SIGNATURE;
		this.version = 3;
		this.depth = 1;
		this.sides = 1;
		this.jpgChannelInfos = new StructJpgChannel[4];
		for (int i = 0; i < this.jpgChannelInfos.length; ++i) {
			this.jpgChannelInfos[i] = new StructJpgChannel();
			this.jpgChannelInfos[i].hasDefaultColor(false);
			this.jpgChannelInfos[i].setQuality(100);
		}
		this.mipmapSizes = new int[13];
	}

	public StructTextureFileHeader(BinaryReader reader) {
		final long headerStart = reader.getPosition();
		final long headerEnd = headerStart + SIZE_IN_BYTES;

		this.signature = reader.readInt32();
		if (this.signature != StructTextureFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("Texture Header", SIGNATURE, this.signature);
		}

		this.version = reader.readInt32();
		this.width = reader.readInt32();
		this.height = reader.readInt32();
		this.depth = reader.readInt32();
		this.sides = reader.readInt32();
		this.mipMaps = reader.readInt32();
		this.format = reader.readInt32();

		this.isJpg = reader.readInt32() != 0;
		this.jpgFormat = reader.readInt32();

		this.jpgChannelInfos = new StructJpgChannel[4];
		for (int i = 0; i < this.jpgChannelInfos.length; ++i) {
			this.jpgChannelInfos[i] = new StructJpgChannel(reader.readInt8(), reader.readInt8(), reader.readInt8());
			if (this.jpgChannelInfos[i].getQuality() < 0 || 100 < this.jpgChannelInfos[i].getQuality()) {
				throw new StructException(this.jpgChannelInfos[i].toString());
			}
		}

		this.mipmapSizesCount = reader.readInt32();
		if (this.mipmapSizesCount > 12) {
			throw new StructException("Number of compressed mips out of bound.");
		}

		this.mipmapSizes = new int[13];
		for (int i = 0; i < this.mipmapSizes.length; ++i) {
			this.mipmapSizes[i] = reader.readInt32();
		}

		this.unk_06C = reader.readInt32();

		if (reader.getPosition() != headerEnd) {
			throw new StructException("Expected number of bytes " + SIZE_IN_BYTES + " read bytes: " + (reader.getPosition() - headerStart));
		}
	}

	public StructJpgChannel getLayer(int idx) {
		return this.jpgChannelInfos[idx];
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructTextureFileHeader [signature=");
		builder.append(this.signature);
		builder.append(", version=");
		builder.append(this.version);
		builder.append(", width=");
		builder.append(this.width);
		builder.append(", height=");
		builder.append(this.height);
		builder.append(", depth=");
		builder.append(this.depth);
		builder.append(", sides=");
		builder.append(this.sides);
		builder.append(", mipMaps=");
		builder.append(this.mipMaps);
		builder.append(", format=");
		builder.append(this.format);
		builder.append(", isCompressed=");
		builder.append(this.isJpg);
		builder.append(", compressionFormat=");
		builder.append(this.jpgFormat);
		builder.append(", layerInfos=");
		builder.append(Arrays.toString(this.jpgChannelInfos));
		builder.append(", imageSizesCount=");
		builder.append(this.mipmapSizesCount);
		builder.append(", imageSizes=");
		builder.append(Arrays.toString(this.mipmapSizes));
		builder.append(", unk_06C=");
		builder.append(this.unk_06C);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.mipmapSizes);
		result = prime * result + Arrays.hashCode(this.jpgChannelInfos);
		result = prime * result + Objects.hash(this.jpgFormat, this.depth, this.format, this.height, this.mipmapSizesCount, this.isJpg, this.mipMaps,
				this.sides, this.signature, this.unk_06C, this.version, this.width);
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
		final StructTextureFileHeader other = (StructTextureFileHeader) obj;
		return this.jpgFormat == other.jpgFormat && this.depth == other.depth && this.format == other.format && this.height == other.height
				&& Arrays.equals(this.mipmapSizes, other.mipmapSizes) && this.mipmapSizesCount == other.mipmapSizesCount && this.isJpg == other.isJpg
				&& Arrays.equals(this.jpgChannelInfos, other.jpgChannelInfos) && this.mipMaps == other.mipMaps && this.sides == other.sides
				&& this.signature == other.signature && this.unk_06C == other.unk_06C && this.version == other.version && this.width == other.width;
	}

}
