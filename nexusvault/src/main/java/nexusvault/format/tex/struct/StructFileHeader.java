package nexusvault.format.tex.struct;

import static kreed.reflection.struct.DataType.BIT_32;
import static kreed.reflection.struct.DataType.STRUCT;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import kreed.reflection.struct.Order;
import kreed.reflection.struct.StructField;
import kreed.reflection.struct.StructUtil;
import nexusvault.shared.ReadAndWritable;
import nexusvault.shared.exception.StructException;

public final class StructFileHeader implements ReadAndWritable {

	public static final String STR_SIGNATURE = "GFX";
	public static final int SIGNATURE = 'G' << 16 | 'F' << 8 | 'X';
	public static final int SIZE_IN_BYTES = StructUtil.sizeOf(StructFileHeader.class); // 0x70;

	static {
		if (SIZE_IN_BYTES != 0x70) {
			throw new StructException();
		}
	}

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

	public StructFileHeader() {
		this.signature = SIGNATURE;
		this.jpgChannelInfos = new StructJpgChannel[4];
		for (int i = 0; i < this.jpgChannelInfos.length; ++i) {
			this.jpgChannelInfos[i] = new StructJpgChannel();
		}
		this.mipmapSizes = new int[13];
	}

	public StructFileHeader(BinaryReader reader) {
		read(reader);
	}

	@Override
	public void read(BinaryReader reader) {
		this.signature = reader.readInt32();
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
			this.jpgChannelInfos[i] = new StructJpgChannel(reader);
		}
		this.mipmapSizesCount = reader.readInt32();
		this.mipmapSizes = new int[13];
		for (int i = 0; i < this.mipmapSizes.length; ++i) {
			this.mipmapSizes[i] = reader.readInt32();
		}
		if (this.mipmapSizesCount > this.mipmapSizes.length) {
			throw new StructException("Number of compressed mips out of bound.");
		}
		this.unk_06C = reader.readInt32();
	}

	@Override
	public void write(BinaryWriter writer) {
		writer.writeInt32(this.signature);
		writer.writeInt32(this.version);
		writer.writeInt32(this.width);
		writer.writeInt32(this.height);
		writer.writeInt32(this.depth);
		writer.writeInt32(this.sides);
		writer.writeInt32(this.mipMaps);
		writer.writeInt32(this.format);
		writer.writeInt32(this.isJpg ? 1 : 0);
		writer.writeInt32(this.jpgFormat);
		for (final StructJpgChannel jpgChannelInfo : this.jpgChannelInfos) {
			jpgChannelInfo.write(writer);
		}
		writer.writeInt32(this.mipmapSizesCount);
		for (final int mipmapSize : this.mipmapSizes) {
			writer.writeInt32(mipmapSize);
		}
		writer.writeInt32(this.unk_06C);
	}

}
