package nexusvault.format.tex.jpg;

import nexusvault.format.tex.jpg.tool.MathUtil;

final class Type0And2PixelComposition implements PixelCompositionStrategy {
	@Override
	public void composite(byte[] imageData, int imageDataPixelOffset, int pixelLayer1, int pixelLayer2, int pixelLayer3, int pixelLayer4) {
		final int alpha = (pixelLayer1 - (pixelLayer3 >> 1));
		final int beta = MathUtil.clamp(alpha + pixelLayer3, 0, 0xFF);
		final int gamma = MathUtil.clamp(alpha - (pixelLayer2 >> 1), 0, 0xFF);
		final int delta = MathUtil.clamp(gamma + pixelLayer2, 0, 0xFF);
		imageData[imageDataPixelOffset + 0] = (byte) MathUtil.clamp(pixelLayer4, 0, 0xFF);
		imageData[imageDataPixelOffset + 1] = (byte) MathUtil.clamp(delta, 0, 0xFF);
		imageData[imageDataPixelOffset + 2] = (byte) MathUtil.clamp(beta, 0, 0xFF);
		imageData[imageDataPixelOffset + 3] = (byte) MathUtil.clamp(gamma, 0, 0xFF);
	}

}