package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.format.tex.dxt.DXTTextureDataEncoder;
import nexusvault.format.tex.jpg.JPG0TextureDataDecoder;
import nexusvault.format.tex.jpg.JPG1TextureDataDecoder;
import nexusvault.format.tex.jpg.JPG2TextureDataDecoder;
import nexusvault.format.tex.struct.StructTextureFileHeader;
import nexusvault.format.tex.unc.ARGB8888TextureDataDecoder;
import nexusvault.format.tex.unc.Gray8TextureDataDecoder;
import nexusvault.format.tex.unc.RGB565TextureDataDecoder;
import nexusvault.shared.exception.IntegerOverflowException;

/**
 * Digests <tt>.tex</tt> data and produces a {@link TextureObject}
 */
public final class TextureReader {

	/**
	 * Builds a {@link TextureReader} and registers the default set of decoders which should be able to decode any texture formats.
	 * <p>
	 * Registered decoders are:
	 * <ul>
	 * <li>{@link JPG0TextureDataDecoder}
	 * <li>{@link JPG1TextureDataDecoder}
	 * <li>{@link JPG2TextureDataDecoder}
	 * <li>{@link DXTTextureDataEncoder}
	 * <li>{@link ARGB8888TextureDataDecoder}
	 * <li>{@link RGB565TextureDataDecoder}
	 * <li>{@link Gray8TextureDataDecoder}
	 * </ul>
	 *
	 * To create a {@link TextureReader} without any decoder use the default {@link TextureReader#TextureReader() constructor}
	 *
	 * @return
	 */
	public static TextureReader buildDefault() {
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

	/**
	 * Creates a {@link TextureReader} without any registered decoder. By default, this reader can not read any texture formats. To retrieve a reader with the
	 * default set of decoder use {@link #buildDefault()}. <br>
	 * To make a decoder useable it is necessary to {@link #registerDecoder(TextureDataDecoder) register} it.
	 */
	public TextureReader() {
		decoders = new ArrayList<>();
	}

	/**
	 * Registers a new decoder. Does not check for duplicates.
	 */
	public void registerDecoder(TextureDataDecoder decoder) {
		if (decoder == null) {
			throw new IllegalArgumentException("'decoder' must not be null");
		}
		decoders.add(decoder);
	}

	public void registerDecoder(int idx, TextureDataDecoder decoder) {
		if (decoder == null) {
			throw new IllegalArgumentException("'decoder' must not be null");
		}
		if (idx >= getDecoderCount()) {
			decoders.add(decoder);
		} else {
			decoders.add(idx, decoder);
		}
	}

	public TextureDataDecoder getDecoder(int idx) {
		return decoders.get(idx);
	}

	public void removeDecoder(int idx) {
		decoders.remove(idx);
	}

	public int getDecoderCount() {
		return decoders.size();
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
