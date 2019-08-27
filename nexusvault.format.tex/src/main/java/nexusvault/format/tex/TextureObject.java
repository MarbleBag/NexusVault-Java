package nexusvault.format.tex;

import java.util.List;

import nexusvault.format.tex.struct.StructTextureFileHeader;

public class TextureObject {

	private final static TextureImageSplitter splitter = new TextureImageSplitter();

	private final StructTextureFileHeader header;
	private final TextureRawData data;
	private final TextureDataDecoder interpreter;

	public TextureObject(StructTextureFileHeader header, TextureRawData data, TextureDataDecoder interpreter) {
		super();
		this.header = header;
		this.data = data;
		this.interpreter = interpreter;
	}

	public int getMipMapCount() {
		return header.mipMaps;
	}

	public int getImageWidth() {
		return header.width;
	}

	public int getImageHeight() {
		return header.height;
	}

	public TexType getTextureDataType() {
		return TexType.resolve(header);
	}

	public TextureImageFormat getTextureImageFormat() {
		return interpreter.getImageFormat();
	}

	/**
	 * @param idx
	 *            - mip map index. 0 referes to level 0 (largest image version)
	 * @return unprocessed image data
	 */
	public byte[] getImageData(int idx) {
		if ((idx < 0) || (header.mipMaps < idx)) {
			throw new IndexOutOfBoundsException("Available index range [" + 0 + " and " + (header.mipMaps - 1) + ") was " + idx);
		}
		final int inverseIdx = computeMipIndex(idx);
		return interpreter.getImageData(header, data, inverseIdx);
	}

	/**
	 * @param idx
	 *            - mip map index. 0 referes to level 0 (largest image version)
	 * @return converted image data
	 */
	public TextureImage getImage(int idx) {
		if ((idx < 0) || (header.mipMaps < idx)) {
			throw new IndexOutOfBoundsException("Available index range [" + 0 + " and " + (header.mipMaps - 1) + ") was " + idx);
		}
		final int inverseIdx = computeMipIndex(idx);
		return interpreter.getImage(header, data, inverseIdx);
	}

	public boolean hasImageMultipleComponents() {
		return splitter.isSplitable(header);
	}

	/**
	 * Splits an image into its core components. Some textures are composed of different types of images. It is possible, that the components are not uniform
	 * and depend actually on some kind of material property / shader. If this is the case, this function will become deprecated and will replaced with
	 * something more appropriated.
	 *
	 * @param image
	 *            to split
	 * @return A list containing its components. List will be empty for unsplittable images.
	 *
	 * @see #hasImageMultipleComponents()
	 */
	public List<TextureImage> splitImageIntoComponents(TextureImage image) {
		return splitter.split(image, header);
	}

	private int computeMipIndex(int idx) {
		return header.mipMaps - 1 - idx;
	}

}
