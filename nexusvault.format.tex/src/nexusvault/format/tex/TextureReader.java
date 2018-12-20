package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.format.tex.dxt.DXTTextureDataEncoder;
import nexusvault.format.tex.jpg.JPG0TextureDataDecoder;
import nexusvault.format.tex.jpg.JPG1TextureDataDecoder;
import nexusvault.format.tex.jpg.JPG2TextureDataDecoder;
import nexusvault.format.tex.unc.ARGB8888TextureDataDecoder;
import nexusvault.format.tex.unc.Gray8TextureDataDecoder;
import nexusvault.format.tex.unc.RGB565TextureDataDecoder;
import nexusvault.shared.exception.IntegerOverflowException;

public final class TextureReader {

	public static TextureReader build() {
		final TextureReader reader = new TextureReader();
		reader.registerDecoder(new JPG0TextureDataDecoder());
		reader.registerDecoder(new JPG1TextureDataDecoder());
		reader.registerDecoder(new JPG2TextureDataDecoder());
		reader.registerDecoder(new DXTTextureDataEncoder());
		reader.registerDecoder(new ARGB8888TextureDataDecoder());
		reader.registerDecoder(new RGB565TextureDataDecoder());
		reader.registerDecoder(new Gray8TextureDataDecoder());
		return reader;
	}

	private final List<TextureDataDecoder> decoders;

	public TextureReader() {
		decoders = new LinkedList<>();
	}

	public void registerDecoder(TextureDataDecoder decoder) {
		if (decoder == null) {
			throw new IllegalArgumentException("'decoder' must not be null");
		}
		decoders.add(decoder);
	}

	public TextureObject read(ByteBuffer byteBuffer) {
		return read(new ByteBufferBinaryReader(byteBuffer));
	}

	public TextureObject read(BinaryReader reader) {
		final StructTextureFileHeader header = new StructTextureFileHeader(reader);

		final TextureDataDecoder decoder = findInterpreter(header);

		if (decoder == null) {
			throw new TextureDataDecoderNotFoundException();
		}

		final long textureDataSize = decoder.calculateTotalTextureDataSize(header);
		final TextureRawData data = loadTextureData(reader, textureDataSize);

		final TextureObject texture = new TextureObject(header, data, decoder);
		return texture;
	}

	private TextureRawData loadTextureData(BinaryReader reader, long textureDataSize) {

		if ((textureDataSize < 0) || (textureDataSize > Integer.MAX_VALUE)) {
			throw new IntegerOverflowException();
		}

		final byte[] bytes = new byte[(int) textureDataSize];
		reader.readInt8(bytes, 0, bytes.length);
		final TextureRawData data = new TextureRawDataByteArray(bytes, ByteOrder.LITTLE_ENDIAN);

		return data;
	}

	private TextureDataDecoder findInterpreter(StructTextureFileHeader header) {
		for (final TextureDataDecoder interpreter : decoders) {
			if (interpreter.accepts(header)) {
				return interpreter;
			}
		}
		return null;
	}

	public int getFileSignature() {
		return StructTextureFileHeader.SIGNATURE;
	}

	public boolean acceptFileSignature(int signature) {
		return getFileSignature() != signature;
	}

	public boolean acceptFileVersion(int version) {
		return true;
	}

}
