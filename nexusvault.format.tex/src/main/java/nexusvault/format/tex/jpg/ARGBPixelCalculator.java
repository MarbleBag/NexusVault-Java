package nexusvault.format.tex.jpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusvault.format.tex.TextureChannel;
import nexusvault.format.tex.TextureChannelFormat;
import nexusvault.format.tex.TextureChannelType;
import nexusvault.format.tex.TextureImage;

public final class ARGBPixelCalculator implements PixelCompositionStrategy {
	private static final List<TextureChannelInfo> channels;
	static {
		final List<TextureChannelInfo> tmp = new ArrayList<>(2);
		tmp.add(new TextureChannelInfo(TextureChannelFormat.ARGB, TextureChannelType.UNKNOWN));
		channels = Collections.unmodifiableList(tmp);
	}

	private final int idxA;
	private final int idxB;
	private final int idxC;
	private final int idxD;

	public ARGBPixelCalculator() {
		this(0, 1, 2, 3);
	}

	public ARGBPixelCalculator(int idxA, int idxB, int idxC, int idxD) {
		if ((idxA < -1) || (idxA > 3)) {
			throw new IllegalArgumentException("'idxA' allowed range is [0,3]");
		}
		if ((idxB < -1) || (idxB > 3)) {
			throw new IllegalArgumentException("'idxB' allowed range is [0,3]");
		}
		if ((idxC < -1) || (idxC > 3)) {
			throw new IllegalArgumentException("'idxC' allowed range is [0,3]");
		}
		if ((idxD < -1) || (idxD > 3)) {
			throw new IllegalArgumentException("'idxD' allowed range is [0,3]");
		}

		if (idxA != -1) {
			if ((idxA == idxB) || (idxA == idxC) || (idxA == idxD)) {
				throw new IllegalArgumentException("'idxA' is duplicate");
			}
		}
		if (idxB != -1) {
			if ((idxB == idxA) || (idxB == idxC) || (idxB == idxD)) {
				throw new IllegalArgumentException("'idxB' is duplicate");
			}
		}
		if (idxC != -1) {
			if ((idxC == idxA) || (idxC == idxB) || (idxC == idxD)) {
				throw new IllegalArgumentException("'idxC' is duplicate");
			}
		}
		if (idxD != -1) {
			if ((idxD == idxA) || (idxD == idxB) || (idxD == idxC)) {
				throw new IllegalArgumentException("'idxD' is duplicate");
			}
		}

		this.idxA = idxA;
		this.idxB = idxB;
		this.idxC = idxC;
		this.idxD = idxD;
	}

	@Override
	public List<TextureChannelInfo> getTextureChannelInfo() {
		return channels;
	}

	@Override
	public void composite(TextureImage out, int outPixelOffset, int normalA, int normalB, int normalC, int normalD) {
		final TextureChannel channel1 = out.getChannel(0);
		if (idxA != -1) {
			channel1.data[(outPixelOffset * 4) + idxA] = (byte) MathUtil.clamp(normalA, 0, 0xFF);
		}
		if (idxB != -1) {
			channel1.data[(outPixelOffset * 4) + idxB] = (byte) MathUtil.clamp(normalB, 0, 0xFF);
		}
		if (idxC != -1) {
			channel1.data[(outPixelOffset * 4) + idxC] = (byte) MathUtil.clamp(normalC, 0, 0xFF);
		}
		if (idxD != -1) {
			channel1.data[(outPixelOffset * 4) + idxD] = (byte) MathUtil.clamp(normalD, 0, 0xFF);
		}
	}
}
