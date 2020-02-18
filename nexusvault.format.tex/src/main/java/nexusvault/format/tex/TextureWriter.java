package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import nexusvault.format.tex.dxt.DXTTextureImageWriter;
import nexusvault.format.tex.jpg.JPGTextureImageWriter;
import nexusvault.format.tex.unc.UncompressedTextureImageWriter;

public final class TextureWriter {

	/**
	 * Builds a {@link TextureWriter} and registers the default set of image writers which should be able to write any texture formats.
	 * <p>
	 * Registered readers are:
	 * <ul>
	 * <li>{@link JPGTextureImageWriter}
	 * <li>{@link DXTTextureImageWriter}
	 * <li>{@link UncompressedTextureImageWriter}
	 * </ul>
	 * <p>
	 * <b>Note:</b> While listed, the jpg writer will not work in a lot of cases
	 * <p>
	 *
	 * To create a {@link TextureWriter} without any writers use the default {@link TextureWriter#TextureWriter() constructor}
	 *
	 * @return A {@link TextureWriter} with a set of default writers
	 */
	public static TextureWriter buildDefault() {
		final TextureWriter reader = new TextureWriter();
		reader.registerImageWriter(new JPGTextureImageWriter());
		reader.registerImageWriter(new DXTTextureImageWriter());
		reader.registerImageWriter(new UncompressedTextureImageWriter());
		return reader;
	}

	private final Map<TexType, TextureImageWriter> writerByType = new HashMap<>();

	/**
	 * Creates a {@link TextureWriter} without any registered encoder. By default, this writer can not write any texture formats. To retrieve a write with the
	 * default set of encoder use {@link #buildDefault()}. <br>
	 * To make a encoder useable it is necessary to {@link #registerImageWriter(TextureImageWriter) register} it.
	 */
	public TextureWriter() {

	}

	/**
	 * Registers a new encoder. Does not check for duplicates.
	 *
	 * @param encoder
	 *            the encoder to register
	 */
	public void registerImageWriter(TextureImageWriter writer) {
		if (writer == null) {
			throw new IllegalArgumentException("'writer' must not be null");
		}
		final var types = writer.getAcceptedTexTypes();
		types.forEach(t -> this.writerByType.put(t, writer));
	}

	public TextureImageWriter getImageWriter(TexType type) {
		return this.writerByType.get(type);
	}

	public void removeImageWriter(TexType type) {
		this.writerByType.remove(type);
	}

	public int getImageWriterCount() {
		return this.writerByType.size();
	}

	/**
	 * Turns the given set of mipmaps into the specified {@link TexType} and returns a WS compatible <i>.tex</i> file. <br>
	 * The mipmaps need to be correctly sorted from smallest to largest.
	 * <p>
	 * <b>Note:</b> <br>
	 * This class may be changed in the future to encompass additional options for building <i>.tex</i> files to make 3D textures possible.
	 *
	 * @return a binary .tex file which can be saved to disk
	 */
	public ByteBuffer write(TexType targetFormat, TextureImage[] mipmaps) {
		final var writer = getImageWriter(targetFormat);
		if (writer == null) {
			throw new TextureDataEncoderNotFoundException();
		}
		final var result = writer.writeTexture(targetFormat, mipmaps);
		return result;
	}

}
