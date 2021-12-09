package nexusvault.format.tex;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
	 * @param writer
	 *            the writer to register
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
	 * Turns the given set of images into the specified {@link TexType} and returns a WS compatible <i>.tex</i> file in binary format. <br>
	 * A image must have a height and width which is a multiple of 2 and not smaller than 1.
	 * <p>
	 * When more than one image is provided, they are used for mip mapping. <br>
	 * <ul>
	 * <li>The images will be sorted automatically by dimension
	 * <li>Each image in the sequence must be half of the dimension of the previous image and can't be smaller than 1x1.
	 * </ul>
	 *
	 * <p>
	 * <b>Example:</b><br>
	 * The first image has a dimension of 1024 x 1024 pixel. The next smaller image in the mipmap set needs to be 512 x 512 pixel.
	 * <p>
	 * <b>Note:</b> <br>
	 * This class may be changed in the future to encompass additional options for building <i>.tex</i> files to make 3D textures possible.
	 *
	 * @param targetFormat
	 *            the target format of the file
	 * @param image
	 *            images to write, their dimension needs to be a power of 2
	 * @param config
	 *            settings, used in writing the image. Which settings are used depends on the writer. For config options see each writer
	 * @return a binary .tex file which can be saved to disk
	 * @see TexType
	 * @see DXTTextureImageWriter
	 * @see JPGTextureImageWriter
	 * @see UncompressedTextureImageWriter
	 * @exception TextureDataEncoderNotFoundException
	 *                if no writer is available for <i>targetFormat</i>
	 * @exception IllegalArgumentException
	 *                if an argument related exception is thrown
	 * @exception TextureException
	 *                if a texture related exception is thrown
	 */
	public ByteBuffer write(TexType targetFormat, TextureImage[] image, Map<String, Object> config) {

		final var writer = getImageWriter(targetFormat);
		if (writer == null) {
			throw new TextureDataEncoderNotFoundException();
		}

		image = sort(image);
		if (image.length < 1) {
			throw new IllegalArgumentException("image needs to be of length 1 or greater");
		}

		config = Objects.requireNonNullElse(config, Collections.emptyMap());
		final var result = writer.writeTexture(targetFormat, image, config);
		return result;
	}

	private TextureImage[] sort(TextureImage[] mipmaps) {
		final var copy = new TextureImage[mipmaps.length];
		System.arraycopy(mipmaps, 0, copy, 0, copy.length);
		Arrays.sort(copy, (a, b) -> {
			final int order = a.getImageHeight() * a.getImageWidth() - b.getImageHeight() * b.getImageWidth();
			return order; // smallest to largest
		});
		return copy;
	}

}
