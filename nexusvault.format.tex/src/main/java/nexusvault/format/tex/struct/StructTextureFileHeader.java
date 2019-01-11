package nexusvault.format.tex.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.STRUCT;

import java.util.Arrays;

import kreed.io.util.BinaryReader;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.exception.IntegerOverflowException;
import nexusvault.shared.exception.SignatureMismatchException;

public final class StructTextureFileHeader {

	public static final String STR_SIGNATURE = "GFX";
	public static final int SIGNATURE = ('G' << 16) | ('F' << 8) | 'X';

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
	 * <li>0 = uncompressed ( BGRA8 )
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
	public boolean isCompressed; // 0x20

	@Order(10)
	@StructField(BIT_32)
	public int compressionFormat; // 0x24

	@Order(11)
	@StructField(value = STRUCT, length = 4)
	public StructLayerInfo[] layerInfos; // 0x28

	@Order(12)
	@StructField(BIT_32)
	public int imageSizesCount; // 0x34

	// public final int lengthOfCompressedImageSize;
	@Order(13)
	@StructField(value = BIT_32, length = 13)
	public int[] imageSizes; // 0x38

	@Order(14)
	@StructField(BIT_32)
	public int unk_06C; // 0x6C

	public StructTextureFileHeader() {
	}

	public StructTextureFileHeader(BinaryReader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("reader must not be null");
		}

		final long headerStart = reader.getPosition();
		final long headerEnd = headerStart + SIZE_IN_BYTES;

		signature = reader.readInt32();
		if (signature != StructTextureFileHeader.SIGNATURE) {
			throw new SignatureMismatchException("Texture Header", SIGNATURE, signature);
		}

		version = reader.readInt32();
		width = reader.readInt32();
		height = reader.readInt32();
		depth = reader.readInt32();
		sides = reader.readInt32();
		mipMaps = reader.readInt32();
		format = reader.readInt32();

		isCompressed = reader.readInt32() != 0;
		compressionFormat = reader.readInt32();

		layerInfos = new StructLayerInfo[4];
		for (int i = 0; i < layerInfos.length; ++i) {
			layerInfos[i] = new StructLayerInfo(reader.readInt8(), reader.readInt8(), reader.readInt8());
			if ((layerInfos[i].getQuality() < 0) || (100 < layerInfos[i].getQuality())) {
				throw new IntegerOverflowException(layerInfos[i].toString());
			}
		}

		imageSizesCount = reader.readInt32();
		if (imageSizesCount > 12) {
			throw new IllegalStateException("Number of compressed mips out of bound.");
		}

		imageSizes = new int[13];
		for (int i = 0; i < imageSizes.length; ++i) {
			imageSizes[i] = reader.readInt32();
		}

		this.unk_06C = reader.readInt32();

		if (reader.getPosition() != headerEnd) {
			throw new IllegalStateException("Expected number of bytes " + SIZE_IN_BYTES + " read bytes: " + (reader.getPosition() - headerStart));
		}
	}

	public StructLayerInfo getLayer(int idx) {
		return layerInfos[idx];
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("StructTextureFileHeader [signature=");
		builder.append(signature);
		builder.append(", version=");
		builder.append(version);
		builder.append(", width=");
		builder.append(width);
		builder.append(", height=");
		builder.append(height);
		builder.append(", depth=");
		builder.append(depth);
		builder.append(", sides=");
		builder.append(sides);
		builder.append(", mipMaps=");
		builder.append(mipMaps);
		builder.append(", format=");
		builder.append(format);
		builder.append(", isCompressed=");
		builder.append(isCompressed);
		builder.append(", compressionFormat=");
		builder.append(compressionFormat);
		builder.append(", layerInfos=");
		builder.append(Arrays.toString(layerInfos));
		builder.append(", imageSizesCount=");
		builder.append(imageSizesCount);
		builder.append(", imageSizes=");
		builder.append(Arrays.toString(imageSizes));
		builder.append(", unk_06C=");
		builder.append(unk_06C);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + compressionFormat;
		result = (prime * result) + depth;
		result = (prime * result) + format;
		result = (prime * result) + height;
		result = (prime * result) + Arrays.hashCode(imageSizes);
		result = (prime * result) + imageSizesCount;
		result = (prime * result) + (isCompressed ? 1231 : 1237);
		result = (prime * result) + Arrays.hashCode(layerInfos);
		result = (prime * result) + mipMaps;
		result = (prime * result) + sides;
		result = (prime * result) + signature;
		result = (prime * result) + unk_06C;
		result = (prime * result) + version;
		result = (prime * result) + width;
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
		if (compressionFormat != other.compressionFormat) {
			return false;
		}
		if (depth != other.depth) {
			return false;
		}
		if (format != other.format) {
			return false;
		}
		if (height != other.height) {
			return false;
		}
		if (!Arrays.equals(imageSizes, other.imageSizes)) {
			return false;
		}
		if (imageSizesCount != other.imageSizesCount) {
			return false;
		}
		if (isCompressed != other.isCompressed) {
			return false;
		}
		if (!Arrays.equals(layerInfos, other.layerInfos)) {
			return false;
		}
		if (mipMaps != other.mipMaps) {
			return false;
		}
		if (sides != other.sides) {
			return false;
		}
		if (signature != other.signature) {
			return false;
		}
		if (unk_06C != other.unk_06C) {
			return false;
		}
		if (version != other.version) {
			return false;
		}
		if (width != other.width) {
			return false;
		}
		return true;
	}

}
