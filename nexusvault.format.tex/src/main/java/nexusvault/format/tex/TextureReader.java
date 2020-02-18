package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteArrayBinaryReader;
import kreed.io.util.ByteBufferBinaryReader;
import nexusvault.format.tex.dxt.DXTTextureImageReader;
import nexusvault.format.tex.jpg.JPGTextureImageReader;
import nexusvault.format.tex.jpg.JPGTextureImageReader2;
import nexusvault.format.tex.struct.StructTextureFileHeader;
import nexusvault.format.tex.unc.ARGB8888TextureImageReader;
import nexusvault.format.tex.unc.Gray8TextureImageReader;
import nexusvault.format.tex.unc.RGB565TextureImageReader;
import nexusvault.shared.exception.IntegerOverflowException;

/**
 * Digests <code>.tex</code> data and produces a {@link TextureObject}
 */
public final class TextureReader {

	/**
	 * Builds a {@link TextureReader} and registers the default set of image readers which should be able to read any texture formats.
	 * <p>
	 * Registered readers are:
	 * <ul>
	 * <li>{@link Gray8TextureImageReader}
	 * <li>{@link RGB565TextureImageReader}
	 * <li>{@link ARGB8888TextureImageReader}
	 * <li>{@link DXTTextureImageReader}
	 * <li>{@link JPGTextureImageReader}
	 * </ul>
	 *
	 * To create a {@link TextureReader} without any readers use the default {@link TextureReader#TextureReader() constructor}
	 *
	 * @return A {@link TextureReader} with a set of default readers
	 */
	public static TextureReader buildDefault() {
		final TextureReader reader = new TextureReader();
		reader.registerImageReader(new Gray8TextureImageReader());
		reader.registerImageReader(new RGB565TextureImageReader());
		reader.registerImageReader(new ARGB8888TextureImageReader());
		reader.registerImageReader(new DXTTextureImageReader());
		reader.registerImageReader(new JPGTextureImageReader2());
		return reader;
	}

	private final Map<TexType, TextureImageReader> readerByType = new HashMap<>();

	/**
	 * Creates a {@link TextureReader} without any registered decoder. By default, this reader can not read any texture formats. To retrieve a reader with the
	 * default set of decoder use {@link #buildDefault()}. <br>
	 * To make a decoder useable it is necessary to {@link #registerImageReader(TextureImageReader) register} it.
	 */
	public TextureReader() {

	}

	/**
	 * Registers a new decoder. Does not check for duplicates.
	 *
	 * @param decoder
	 *            the decoder to register
	 */
	public void registerImageReader(TextureImageReader reader) {
		if (reader == null) {
			throw new IllegalArgumentException("'reader' must not be null");
		}
		final var types = reader.getAcceptedTexTypes();
		types.forEach(t -> this.readerByType.put(t, reader));
	}

	public TextureImageReader getImageReader(TexType type) {
		return this.readerByType.get(type);
	}

	public void removeImageReader(TexType type) {
		this.readerByType.remove(type);
	}

	public int getImageReaderCount() {
		return this.readerByType.size();
	}

	public StructTextureFileHeader readHeader(ByteBuffer byteBuffer) {
		return readHeader(new ByteBufferBinaryReader(byteBuffer));
	}

	public StructTextureFileHeader readHeader(BinaryReader reader) {
		return new StructTextureFileHeader(reader);
	}

	public TextureObject read(ByteBuffer byteBuffer) {
		return read(new ByteBufferBinaryReader(byteBuffer));
	}

	public TextureObject read(BinaryReader source) {
		final StructTextureFileHeader header = readHeader(source);
		final TextureImageReader reader = findImageReader(header);

		if (reader == null) {
			throw new TextureDataDecoderNotFoundException();
		}

		final long textureDataSize = reader.calculateExpectedTextureImageSize(header);
		final BinaryReader data = loadTextureData(source, textureDataSize);

		final TextureObject texture = new DecodeableTextureObject(header, data, reader);
		return texture;
	}

	private BinaryReader loadTextureData(BinaryReader reader, long textureDataSize) {

		if (textureDataSize < 0 || textureDataSize > Integer.MAX_VALUE) {
			throw new IntegerOverflowException();
		}

		final byte[] bytes = new byte[(int) textureDataSize];
		reader.readInt8(bytes, 0, bytes.length);
		final BinaryReader data = new ByteArrayBinaryReader(bytes, ByteOrder.LITTLE_ENDIAN);
		// final TextureRawData data = new TextureRawDataByteArray(bytes, ByteOrder.LITTLE_ENDIAN);
		return data;
	}

	private TextureImageReader findImageReader(StructTextureFileHeader header) {
		final var texType = TexType.resolve(header);
		return this.readerByType.get(texType);
	}

	public int getFileSignature() {
		return StructTextureFileHeader.SIGNATURE;
	}

	public boolean acceptFileSignature(int signature) {
		return getFileSignature() == signature;
	}

	public boolean acceptFileVersion(int version) {
		return true;
	}

}
