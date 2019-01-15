package nexusvault.format.tex.jpg;

public final class Type0And2PixelComposition implements PixelCompositionStrategy {
	@Override
	public void composite(byte[] imageData, int imageDataPixelOffset, int pixelA, int pixelB, int pixelC, int pixelD) {
		final int alpha = (pixelA - (pixelC >> 1));
		final int beta = MathUtil.clamp(alpha + pixelC, 0, 0xFF);
		final int gamma = MathUtil.clamp(alpha - (pixelB >> 1), 0, 0xFF);
		final int delta = MathUtil.clamp(gamma + pixelB, 0, 0xFF);
		imageData[imageDataPixelOffset + 0] = (byte) MathUtil.clamp(pixelD, 0, 0xFF);
		imageData[imageDataPixelOffset + 1] = (byte) MathUtil.clamp(delta, 0, 0xFF);
		imageData[imageDataPixelOffset + 2] = (byte) MathUtil.clamp(beta, 0, 0xFF);
		imageData[imageDataPixelOffset + 3] = (byte) MathUtil.clamp(gamma, 0, 0xFF);
	}

}