package nexusvault.format.tex;

public abstract class AbstTextureDataDecoder implements TextureDataDecoder {

	protected abstract ImageMetaInformation getImageInformation(StructTextureFileHeader header, int idx);

	protected abstract TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx);

	@Override
	public long calculateTotalTextureDataSize(StructTextureFileHeader header) {
		final ImageMetaInformation info = getImageInformation(header, header.mipMaps - 1);
		return info.offset + info.length;
	}

	@Override
	public byte[] getImageData(StructTextureFileHeader header, TextureRawData data, int idx) {
		final ImageMetaInformation meta = getImageInformation(header, idx);
		final byte[] store = new byte[meta.length];
		data.copyTo(meta.offset, store, 0, meta.length);
		return store;
	}

	@Override
	public TextureImage getImage(StructTextureFileHeader header, TextureRawData data, int idx) {
		final ImageMetaInformation meta = getImageInformation(header, idx);
		return getImage(header, data, meta, idx);
	}

}
