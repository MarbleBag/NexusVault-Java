package nexusvault.format.tex.jpg;

import nexusvault.format.tex.jpg.JPGDecoderBase.PixelCompositionProvider;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class DefaultPixelCompositionProvider implements PixelCompositionProvider {

	@Override
	public PixelCompositionStrategy getPixelCalculator(StructTextureFileHeader header) {
		final int format = header.compressionFormat;
		switch (format) {
			case 0:
			case 2:
				return new Type0And2PixelComposition();
			case 1:
				return new Type1PixelComposition();
		}
		return null;
	}

}
