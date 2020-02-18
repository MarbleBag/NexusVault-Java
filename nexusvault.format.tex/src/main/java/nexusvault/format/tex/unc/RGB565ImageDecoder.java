package nexusvault.format.tex.unc;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import kreed.io.util.BinaryReader;
import nexusvault.format.tex.TexType;
import nexusvault.format.tex.TextureImageFormat;

final class RGB565ImageDecoder implements UncompressedImageDecoder {

	private final Set<TexType> acceptedTypes = Collections.unmodifiableSet(EnumSet.of(TexType.RGB));

	// something may be fishy here

	@Override
	public byte[] decode(BinaryReader source, int byteLength) {
		final int numberOfPixels = byteLength / Short.BYTES; // RGB 565 = 16 bit per pixel
		final byte[] imageData = new byte[TextureImageFormat.RGB.getBytesPerPixel() * numberOfPixels];

		for (int i = 0; i < imageData.length; i += 3) {
			// stuff is stored in little endian, while been seen as B[5]G[3] | G[3]R[5] in file, we get R[5]G[6]B[5] by reading 16bit at once
			final short data = source.readInt16();
			final var r = (data & 0xF800) >> 0xB; // R, extract first 5 bits
			final var g = (data & 0x07E0) >> 0x5; // G, extract mid 6 bits
			final var b = (data & 0x001F) >> 0x0; // B, extract last 5 bits

			// convert via C8 = (C5 * 255) / 31;
			// or C8 = (R5 << 3) | (C5 >> 2);

			imageData[i + 0] = (byte) r;// (r * 0xFF / 0x1F); // r;// (r << 3 | r >> 2); // R
			imageData[i + 1] = (byte) g;// (g * 0xFF / 0x3F); // (g << 2 | g >> 4); // G
			imageData[i + 2] = (byte) b;// (b * 0xFF / 0x1F);// (b << 3 | b >> 2); // B
		}

		return imageData;
	}

	@Override
	public TextureImageFormat getReturnedImageFormat() {
		return TextureImageFormat.RGB;
	}

	@Override
	public Set<TexType> getAcceptedTexTypes() {
		return this.acceptedTypes;
	}

}
