package nexusvault.format.tex.jpg;

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
import nexusvault.format.tex.jpg.old.AllInOneJPGDecoder;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class JPGTextureImageReader extends AbstractTextureImageReader implements TextureImageReader {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.JPEG_TYPE_1, TexType.JPEG_TYPE_2, TexType.JPEG_TYPE_3));
	private final AllInOneJPGDecoder decoder = new AllInOneJPGDecoder();

	public JPGTextureImageReader() {
		super(new JPGImageMetaCalculator());
	}

	@Override
	public TextureImage read(StructTextureFileHeader header, BinaryReader source, int imageIdx) {
		final ImageMetaInformation meta = getImageInformation(header, imageIdx);

		source.seek(Seek.BEGIN, meta.offset);
		final byte[] data = this.decoder.decode(header, source, meta.length, meta.width, meta.height);

		return new TextureImage(meta.width, meta.height, TextureImageFormat.ARGB, data);
	}

	@Override
	public TextureImageFormat getImageFormat() {
		return TextureImageFormat.ARGB;
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return this.acceptedTypes;
	}

}
