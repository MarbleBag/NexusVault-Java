package nexusvault.format.tex.jpg;

import nexusvault.format.tex.jpg.tool.MathUtil;

final class Type1PixelComposition implements PixelCompositionStrategy {

	@Override
	public void composite(byte[] imageData, int imageDataPixelOffset, int pixelLayer1, int pixelLayer2, int pixelLayer3, int pixelLayer4) {
		imageData[imageDataPixelOffset + 0] = (byte) MathUtil.clamp(pixelLayer1, 0, 0xFF);
		imageData[imageDataPixelOffset + 1] = (byte) MathUtil.clamp(pixelLayer2, 0, 0xFF);
		imageData[imageDataPixelOffset + 2] = (byte) MathUtil.clamp(pixelLayer3, 0, 0xFF);
		imageData[imageDataPixelOffset + 3] = (byte) MathUtil.clamp(pixelLayer4, 0, 0xFF);
	}

}