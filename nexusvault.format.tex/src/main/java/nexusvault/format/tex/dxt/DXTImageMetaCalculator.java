package nexusvault.format.tex.dxt;

import nexusvault.format.tex.ImageMetaCalculator;
import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public class DXTImageMetaCalculator implements ImageMetaCalculator {

	@Override
	public ImageMetaInformation getImageInformation(StructTextureFileHeader header, int mipmapIndex) {
		if (header == null) {
			throw new IllegalArgumentException("'header' must not be null");
		}
		if ((mipmapIndex < 0) || (header.mipMaps <= mipmapIndex)) {
			throw new IndexOutOfBoundsException(String.format("'mipmapIndex' out of bounds. Range [0;%d). Got %d", header.mipMaps, mipmapIndex));
		}

		final boolean isCompressed = header.format == 13;
		final int blockSize = isCompressed ? 8 : 16;

		int width = 0, height = 0, length = 0, offset = 0;

		for (int i = 0; i <= mipmapIndex; ++i) {
			width = (int) (header.width / Math.pow(2, header.mipMaps - 1 - i));
			height = (int) (header.height / Math.pow(2, header.mipMaps - 1 - i));
			width = width <= 0 ? 1 : width;
			height = height <= 0 ? 1 : height;

			offset += length;
			length = ((width + 3) / 4) * ((height + 3) / 4) * blockSize;
		}

		return new ImageMetaInformation(offset, length, width, height);
	}

}
