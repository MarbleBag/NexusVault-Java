package nexusvault.format.tex.unc;

import java.util.Objects;
import java.util.Set;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.tex.AbstractTextureImageReader;
import nexusvault.format.tex.ImageMetaCalculator;
import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureImageFormat;
import nexusvault.format.tex.TextureImageReader;
import nexusvault.format.tex.struct.StructTextureFileHeader;

abstract class UncompressedTextureImageReader extends AbstractTextureImageReader implements TextureImageReader {

	private final UncompressedDecoder decoder;

	public UncompressedTextureImageReader(UncompressedDecoder decoder, ImageMetaCalculator imageMeta) {
		super(imageMeta);
		this.decoder = Objects.requireNonNull(decoder);
	}

	@Override
	public TextureImage read(StructTextureFileHeader header, BinaryReader source, int imageIdx) {
		final ImageMetaInformation meta = getImageInformation(header, imageIdx);
		source.seek(Seek.BEGIN, meta.offset);
		final byte[] data = decoder.decode(source, meta.length);
		return new TextureImage(meta.width, meta.height, getImageFormat(), data);
	}

	@Override
	public TextureImageFormat getImageFormat() {
		return decoder.getReturnedImageFormat();
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return decoder.getAcceptedTexTypes();
	}

}
