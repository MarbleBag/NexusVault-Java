package nexusvault.format.tex.jpg;

import nexusvault.format.tex.ImageMetaCalculator;
import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.struct.StructTextureFileHeader;

final class JPGImageMetaCalculator implements ImageMetaCalculator {

	@Override
	public ImageMetaInformation getImageInformation(StructTextureFileHeader header, int mipmapIndex) {
		if (header == null) {
			throw new IllegalArgumentException("'header' must not be null");
		}
		if (mipmapIndex < 0 || header.mipMaps <= mipmapIndex) {
			throw new IndexOutOfBoundsException(String.format("'idx' out of bounds. Range [0;%d). Got %d", header.mipMaps, mipmapIndex));
		}
		if (header.mipMaps != header.mipmapSizesCount) {
			throw new IndexOutOfBoundsException(String.format("'header.imageSizesCount' and 'header.mipMaps' need to be qual. MipMaps: %d, ImageSizeCount: %d",
					header.mipMaps, header.mipmapSizesCount));
		}

		final int length = header.mipmapSizes[mipmapIndex];
		int offset = 0;
		for (int i = 0; i < mipmapIndex; ++i) {
			offset += header.mipmapSizes[i];
		}

		int width = header.width >> header.mipMaps - mipmapIndex - 1;
		int height = header.height >> header.mipMaps - mipmapIndex - 1;
		width = width <= 0 ? 1 : width;
		height = height <= 0 ? 1 : height;

		return new ImageMetaInformation(offset, length, width, height);
	}

}
