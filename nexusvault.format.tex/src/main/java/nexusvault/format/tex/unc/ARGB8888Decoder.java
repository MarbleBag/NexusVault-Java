package nexusvault.format.tex.unc;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;

final class ARGB8888Decoder implements UncompressedDecoder {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.ARGB_1, TexType.ARGB_2));

	@Override
	public byte[] decode(BinaryReader source, int byteLength) {
		final byte[] imageData = new byte[byteLength];
		source.readInt8(imageData, 0, imageData.length);
		for (int i = 0; i < imageData.length; i += 4) { // turns WS's BGRA into ARGB
			final var B = imageData[i + 0];
			final var G = imageData[i + 1];
			final var R = imageData[i + 2];
			final var A = imageData[i + 3];
			imageData[i + 0] = A;
			imageData[i + 1] = R;
			imageData[i + 2] = G;
			imageData[i + 3] = B;
		}
		return imageData;
	}

	@Override
	public TextureImageFormat getReturnedImageFormat() {
		return TextureImageFormat.ARGB;
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return acceptedTypes;
	}

}
