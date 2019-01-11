package nexusvault.format.tex.jpg;

public final class Type0And2PixelComposition implements PixelCompositionStrategy {
	@Override
	public void composite(byte[] imageData, int imageDataPixelOffset, int pixelA, int pixelB, int pixelC, int pixelD) {
		final int alpha = (pixelA - (pixelC >> 1));
		final int beta = MathUtil.clamp(alpha + pixelC, 0, 0xFF);
		final int gamma = MathUtil.clamp(alpha - (pixelB >> 1), 0, 0xFF);
		final int delta = MathUtil.clamp(gamma + pixelB, 0, 0xFF);

		final int g1 = beta;
		final int b1 = gamma;
		final int r1 = delta;

		final int r2 = pixelD;

		imageData[imageDataPixelOffset + 0] = (byte) MathUtil.clamp(r2, 0, 0xFF);
		imageData[imageDataPixelOffset + 1] = (byte) MathUtil.clamp(r1, 0, 0xFF);
		imageData[imageDataPixelOffset + 2] = (byte) MathUtil.clamp(g1, 0, 0xFF);
		imageData[imageDataPixelOffset + 3] = (byte) MathUtil.clamp(b1, 0, 0xFF);
	}

}