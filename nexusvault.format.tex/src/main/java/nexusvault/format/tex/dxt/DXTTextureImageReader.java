package nexusvault.format.tex.dxt;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.github.goldsam.jsquish.Squish;
import com.github.goldsam.jsquish.Squish.CompressionType;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.tex.AbstractTextureImageReader;
import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureImageReader;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class DXTTextureImageReader extends AbstractTextureImageReader implements TextureImageReader {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.DXT1, TexType.DXT3, TexType.DXT5));

	public DXTTextureImageReader() {
		super(new DXTImageMetaCalculator());
	}

	@Override
	public TextureImageFormat getImageFormat() {
		return TextureImageFormat.ARGB;
	}

	@Override
	public TextureImage read(StructTextureFileHeader header, BinaryReader source, int imageIdx) {
		final ImageMetaInformation meta = getImageInformation(header, imageIdx);
		final var texType = TexType.resolve(header);
		final var dxtCompression = getCompressionType(texType);

		source.seek(Seek.BEGIN, meta.offset);
		final byte[] data = decode(dxtCompression, source, meta.length, meta.width, meta.height);

		return new TextureImage(meta.width, meta.height, getImageFormat(), data);
	}

	private byte[] decode(CompressionType dxtCompression, BinaryReader source, int byteLength, int width, int height) {
		final var compressed = new byte[byteLength];
		source.readInt8(compressed, 0, compressed.length);
		final var decompressed = Squish.decompressImage(null, width, height, compressed, dxtCompression);
		convertRGBAToARGB(decompressed);
		return decompressed;
	}

	private void convertRGBAToARGB(byte[] arr) {
		for (int i = 0; i < arr.length; i += 4) {
			final var r = arr[i + 0];
			final var g = arr[i + 1];
			final var b = arr[i + 2];
			final var a = arr[i + 3];
			arr[i + 0] = a;
			arr[i + 1] = r;
			arr[i + 2] = g;
			arr[i + 3] = b;
		}
	}

	private Squish.CompressionType getCompressionType(TexType target) {
		switch (target) {
			case DXT1:
				return Squish.CompressionType.DXT1;
			case DXT3:
				return Squish.CompressionType.DXT3;
			case DXT5:
				return Squish.CompressionType.DXT5;
			default:
				throw new IllegalArgumentException(/* TODO */);
		}
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return this.acceptedTypes;
	}

}
