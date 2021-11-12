package nexusvault.format.tex.unc;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;

/**
 * Thread-Safe
 */
final class Gray8ImageDecoder implements UncompressedImageDecoder {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.GRAYSCALE));

	@Override
	public byte[] decode(BinaryReader source, int byteLength) {
		final byte[] imageData = new byte[byteLength];
		source.readInt8(imageData, 0, imageData.length);
		return imageData;
	}

	@Override
	public TextureImageFormat getReturnedImageFormat() {
		return TextureImageFormat.GRAYSCALE;
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return this.acceptedTypes;
	}

}
