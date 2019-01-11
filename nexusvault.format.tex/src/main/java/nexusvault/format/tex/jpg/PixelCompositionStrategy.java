package nexusvault.format.tex.jpg;

import java.util.List;

import nexusvault.format.tex.TextureImage;

public interface PixelCompositionStrategy {
	public void composite(TextureImage out, int outPixelOffset, int pixelA, int pixelB, int pixelC, int pixelD);

	public List<TextureChannelInfo> getTextureChannelInfo();
}