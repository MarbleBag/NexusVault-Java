package nexusvault.format.tex.jpg;

import java.util.List;

import nexusvault.format.tex.TextureChannelType;
import nexusvault.format.tex.TextureImage;

interface PixelCalculator {
	public void compute(TextureImage out, int outPixelOffset, int pixelA, int pixelB, int pixelC, int pixelD);

	public List<TextureChannelType> getTextureChannelTypes();
}