package nexusvault.format.tex.unc;

import kreed.io.util.BinaryReader;
import kreed.io.util.BinaryWriter;
import nexusvault.format.tex.TextureConversionException;
import nexusvault.format.tex.TextureImageFormat;

final class ARGB8888Encoder implements UncompressedEncoder {

	@Override
	public boolean accepts(TextureImageFormat format) {
		switch (format) {
			case ARGB:
			case RGB:
			case GRAYSCALE:
				return true;
			default:
				return false;
		}
	}

	@Override
	public void encode(BinaryReader source, TextureImageFormat format, int width, int height, BinaryWriter destination) {
		switch (format) {
			case ARGB:
				encodeARGB(source, width, height, destination);
				break;
			case RGB:
				encodeRGB(source, width, height, destination);
				break;
			case GRAYSCALE:
				encodeGray(source, width, height, destination);
				break;
			default:
				throw new TextureConversionException(/* TODO */);
		}
	}

	private void encodeARGB(BinaryReader source, int width, int height, BinaryWriter destination) {
		final byte[] imageData = new byte[TextureImageFormat.ARGB.getBytesPerPixel() * width * height];
		source.readInt8(imageData, 0, imageData.length);
		for (int i = 0; i < imageData.length; i += 4) { // turns ARGB into WS's BGRA
			final var A = imageData[i + 0];
			final var R = imageData[i + 1];
			final var G = imageData[i + 2];
			final var B = imageData[i + 3];
			imageData[i + 0] = B;
			imageData[i + 1] = G;
			imageData[i + 2] = R;
			imageData[i + 3] = A;
		}

		destination.writeInt8(imageData, 0, imageData.length);
	}

	private void encodeRGB(BinaryReader source, int width, int height, BinaryWriter destination) {
		final byte[] imageData = new byte[TextureImageFormat.ARGB.getBytesPerPixel() * width * height];
		final byte[] bufferData = new byte[TextureImageFormat.RGB.getBytesPerPixel() * width * height];
		source.readInt8(bufferData, 0, imageData.length);

		for (int i = 0, k = 0; i < bufferData.length; i += 3, k += 4) { // turns RGB into WS's BGRA
			final byte R = bufferData[i + 0];
			final byte G = bufferData[i + 1];
			final byte B = bufferData[i + 2];
			imageData[k + 0] = B;
			imageData[k + 1] = G;
			imageData[k + 2] = R;
			imageData[k + 3] = (byte) 0xFF;// A
		}

		destination.writeInt8(imageData, 0, imageData.length);
	}

	private void encodeGray(BinaryReader source, int width, int height, BinaryWriter destination) {
		final int length = width * height;
		final byte[] imageData = new byte[TextureImageFormat.ARGB.getBytesPerPixel() * length];
		for (int i = 0; i < imageData.length; i += 4) { // turns RGB into WS's BGRA
			final byte shade = source.readInt8();
			imageData[i + 0] = shade;
			imageData[i + 1] = shade;
			imageData[i + 2] = shade;
			imageData[i + 3] = (byte) 0xFF;// A
		}
		destination.writeInt8(imageData, 0, imageData.length);
	}

}
