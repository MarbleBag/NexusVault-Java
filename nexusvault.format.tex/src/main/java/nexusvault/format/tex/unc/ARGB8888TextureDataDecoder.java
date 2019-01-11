package nexusvault.format.tex.unc;

import nexusvault.format.tex.ImageMetaInformation;
import nexusvault.format.tex.TextureChannel;
import nexusvault.format.tex.TextureChannelFormat;
import nexusvault.format.tex.TextureChannelType;
import nexusvault.format.tex.TextureImage;
import nexusvault.format.tex.TextureRawData;
import nexusvault.format.tex.struct.StructTextureFileHeader;

public final class ARGB8888TextureDataDecoder extends AbstUncompressedTextureDataDecoder {

	final private static int BYTES_PER_PIXEL = 4;

	public ARGB8888TextureDataDecoder() {
		super(BYTES_PER_PIXEL);
	}

	@Override
	public boolean accepts(StructTextureFileHeader header) {
		return super.accepts(header) && ((header.format == 1) || (header.format == 0));
	}

	@Override
	protected TextureImage getImage(StructTextureFileHeader header, TextureRawData data, ImageMetaInformation meta, int idx) {
		final byte[] channelData = new byte[meta.length];
		data.copyTo(meta.offset, channelData, 0, meta.length);
		final TextureChannel channel = new TextureChannel(TextureChannelFormat.ARGB, TextureChannelType.DIFFUSE, channelData);
		return new TextureImage(meta.width, meta.height, channel);
	}

}
