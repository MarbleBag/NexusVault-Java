package nexusvault.format.tex.jpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusvault.format.tex.TextureChannel;
import nexusvault.format.tex.TextureChannelFormat;
import nexusvault.format.tex.TextureChannelType;
import nexusvault.format.tex.TextureImage;

public final class Type1PixelComposition implements PixelCompositionStrategy {
	private static final List<TextureChannelInfo> channels;
	static {
		final List<TextureChannelInfo> tmp = new ArrayList<>(2);
		tmp.add(new TextureChannelInfo(TextureChannelFormat.ARGB, TextureChannelType.NORMAL));
		tmp.add(new TextureChannelInfo(TextureChannelFormat.GRAYSCALE, TextureChannelType.METALLIC));
		tmp.add(new TextureChannelInfo(TextureChannelFormat.GRAYSCALE, TextureChannelType.EMISSION));
		channels = Collections.unmodifiableList(tmp);
	}

	@Override
	public List<TextureChannelInfo> getTextureChannelInfo() {
		return channels;
	}

	@Override
	public void composite(TextureImage out, int outPixelOffset, int normalA, int normalB, int roughness, int emission) {
		final float x = (normalA / 128f) - 1;
		final float y = (normalB / 128f) - 1;
		final float z = (float) Math.sqrt(1 - (x * x) - (y * y));
		final int normalC = Math.round(z * 255);

		final TextureChannel channel1 = out.getChannel(0);
		channel1.data[(outPixelOffset * 4) + 0] = (byte) 0xFF;
		channel1.data[(outPixelOffset * 4) + 1] = (byte) MathUtil.clamp(normalA, 0, 0xFF);
		channel1.data[(outPixelOffset * 4) + 2] = (byte) MathUtil.clamp(normalB, 0, 0xFF);
		channel1.data[(outPixelOffset * 4) + 3] = (byte) MathUtil.clamp(normalC, 0, 0xFF);

		final TextureChannel channel2 = out.getChannel(1);
		channel2.data[outPixelOffset] = (byte) MathUtil.clamp(roughness, 0, 0xFF);

		final TextureChannel channel3 = out.getChannel(2);
		channel3.data[outPixelOffset] = (byte) MathUtil.clamp(emission, 0, 0xFF);
	}

}