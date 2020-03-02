package nexusvault.format.tex;

public interface TextureObject {

	int getMipMapCount();

	int getImageWidth();

	int getImageHeight();

	TexType getTextureDataType();

	TextureImageFormat getTextureImageFormat();

	/**
	 * @param idx
	 *            - mip map index. 0 refers to level 0 (largest image version)
	 * @return unprocessed image data
	 */

	byte[] getImageData(int idx);

	/**
	 * @param idx
	 *            - mip map index. 0 refers to level 0 (largest image version)
	 * @return converted image data
	 */
	TextureImage getImage(int idx);

}
