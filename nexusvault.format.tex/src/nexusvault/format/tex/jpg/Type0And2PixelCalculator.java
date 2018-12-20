package nexusvault.format.tex.jpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusvault.format.tex.TextureChannel;
import nexusvault.format.tex.TextureChannelType;
import nexusvault.format.tex.TextureImage;

class Type0And2PixelCalculator implements PixelCalculator {
	private static final List<TextureChannelType> channels;
	static {
		final List<TextureChannelType> tmp = new ArrayList<>(2);
		tmp.add(TextureChannelType.ARGB);
		tmp.add(TextureChannelType.GRAYSCALE);
		channels = Collections.unmodifiableList(tmp);
	}

	@Override
	public List<TextureChannelType> getTextureChannelTypes() {
		return channels;
	}

	@Override
	public void compute(TextureImage out, int outPixelOffset, int pixelA, int pixelB, int pixelC, int pixelD) {
		final int alpha = (pixelA - (pixelC >> 1));
		final int beta = MathUtil.clamp(alpha + pixelC, 0, 0xFF);
		final int gamma = MathUtil.clamp(alpha - (pixelB >> 1), 0, 0xFF);
		final int delta = MathUtil.clamp(gamma + pixelB, 0, 0xFF);

		final int g1 = beta;
		final int b1 = gamma;
		final int r1 = delta;

		final int r2 = pixelD;

		final TextureChannel channel1 = out.getChannel(0);
		channel1.data[(outPixelOffset * 4) + 0] = (byte) 0xFF;
		channel1.data[(outPixelOffset * 4) + 1] = (byte) MathUtil.clamp(r1, 0, 0xFF);
		channel1.data[(outPixelOffset * 4) + 2] = (byte) MathUtil.clamp(g1, 0, 0xFF);
		channel1.data[(outPixelOffset * 4) + 3] = (byte) MathUtil.clamp(b1, 0, 0xFF);

		final TextureChannel channel2 = out.getChannel(1);
		channel2.data[outPixelOffset] = (byte) MathUtil.clamp(r2, 0, 0xFF);
	}

}