package nexusvault.format.tex.unc;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;

final class RGB565Decoder implements UncompressedDecoder {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.RGB));

	@Override
	public byte[] decode(BinaryReader source, int byteLength) {
		final int numberOfPixels = byteLength / Short.BYTES; // RGB 565 = 16 bit per pixel
		final byte[] imageData = new byte[TextureImageFormat.RGB.getBytesPerPixel() * numberOfPixels];

		for (int i = 0; i < numberOfPixels; ++i) {
			final short data = source.readInt16();
			imageData[i + 0] = (byte) ((data >> 11) & 0x1F); // R
			imageData[i + 1] = (byte) ((data >> 5) & 0x3F); // G
			imageData[i + 2] = (byte) ((data >> 0) & 0x1F); // B
		}

		return imageData;
	}

	@Override
	public TextureImageFormat getReturnedImageFormat() {
		return TextureImageFormat.RGB;
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return acceptedTypes;
	}

}
