package nexusvault.format.tex.jpg;

public final class Type1PixelComposition implements PixelCompositionStrategy {

	@Override
	public void composite(byte[] imageData, int imageDataPixelOffset, int pixelA, int pixelB, int pixelC, int pixelD) {
		imageData[imageDataPixelOffset + 0] = (byte) MathUtil.clamp(pixelA, 0, 0xFF);
		imageData[imageDataPixelOffset + 1] = (byte) MathUtil.clamp(pixelB, 0, 0xFF);
		imageData[imageDataPixelOffset + 2] = (byte) MathUtil.clamp(pixelC, 0, 0xFF);
		imageData[imageDataPixelOffset + 3] = (byte) MathUtil.clamp(pixelD, 0, 0xFF);
	}

}