package nexusvault.format.tex.jpg;

public interface PixelCompositionStrategy {

	/**
	 * @param imageData
	 *            byte grid which will compose the image. Bytes are ordered in ARGB
	 * @param imageDataPixelOffset
	 *            offset at which the composed pixel is to be set. This value points to the A component of the image. The other components will be accessed by
	 *            adding 1, 2 or 3 to this value.
	 * @param pixelLayer1
	 *            pixel from layer 1
	 * @param pixelLayer2
	 *            pixel from layer 2
	 * @param pixelLayer3
	 *            pixel from layer 3
	 * @param pixelLayer4
	 *            pixel from layer 4
	 */
	public void composite(byte[] imageData, int imageDataPixelOffset, int pixelLayer1, int pixelLayer2, int pixelLayer3, int pixelLayer4);

}