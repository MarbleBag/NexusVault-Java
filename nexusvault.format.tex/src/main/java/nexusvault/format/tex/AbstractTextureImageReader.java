package nexusvault.format.tex;

import java.util.Objects;

import kreed.io.util.BinaryReader;
import kreed.io.util.Seek;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public abstract class AbstractTextureImageReader implements TextureImageReader {

	private final ImageMetaCalculator imageMeta;

	public AbstractTextureImageReader(ImageMetaCalculator imageMeta) {
		this.imageMeta = Objects.requireNonNull(imageMeta);
	}

	protected final ImageMetaInformation getImageInformation(StructTextureFileHeader header, int mipmapIndex) {
		return imageMeta.getImageInformation(header, mipmapIndex);
	}

	@Override
	public final long calculateExpectedTextureImageSize(StructTextureFileHeader header) {
		final ImageMetaInformation info = getImageInformation(header, header.mipMaps - 1);
		return info.offset + info.length;
	}

	@Override
	public final byte[] getUnprocessedImageData(StructTextureFileHeader header, BinaryReader source, int imageIdx) {
		final ImageMetaInformation meta = getImageInformation(header, imageIdx);
		final byte[] store = new byte[meta.length];
		source.seek(Seek.BEGIN, meta.offset);
		source.readInt8(store, 0, store.length);
		return store;
	}
}
