package nexusvault.format.tex.dxt;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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

	private final DXTDecoder dxt1decoder = new DXTDecoder(TexType.DXT1);
	private final DXTDecoder dxt3decoder = new DXTDecoder(TexType.DXT3);
	private final DXTDecoder dxt5decoder = new DXTDecoder(TexType.DXT5);

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
		final var decoder = getDecoder(header);

		source.seek(Seek.BEGIN, meta.offset);
		final byte[] data = decoder.decode(source, meta.length, meta.width, meta.height);

		return new TextureImage(meta.width, meta.height, getImageFormat(), data);
	}

	private DXTDecoder getDecoder(StructTextureFileHeader header) {
		final var texType = TexType.resolve(header);
		switch (texType) {
			case DXT1:
				return dxt1decoder;
			case DXT3:
				return dxt3decoder;
			case DXT5:
				return dxt5decoder;
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return acceptedTypes;
	}

}
