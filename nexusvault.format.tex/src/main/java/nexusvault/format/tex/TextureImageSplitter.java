package nexusvault.format.tex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusvault.format.tex.struct.StructTextureFileHeader;

/**
 * Splits an image into its core components. Some textures are composed of different types of images. It is possible, that the components are not uniform and
 * depend actually on some kind of material property / shader. If this is the case, this function will become deprecated and will replaced with something more
 * appropriated.
 */
public final class TextureImageSplitter {

	private static final class TextureSplitInfo {
		public final TextureImageFormat format;
		public final TextureImageExpectedUseType type;

		public TextureSplitInfo(TextureImageFormat format, TextureImageExpectedUseType type) {
			super();
			this.format = format;
			this.type = type;
		}

		public TextureImageFormat getFormat() {
			return this.format;
		}

		public TextureImageExpectedUseType getType() {
			return this.type;
		}
	}

	private static abstract class Splitter {

		public final List<TextureImage> split(TextureImage original) {
			final byte[] originalData = original.getImageData();
			final int imageWidth = original.getImageWidth();
			final int imageHeight = original.getImageHeight();
			final int pixelCount = originalData.length / 4;

			final List<TextureSplitInfo> splitInfos = getSplitInfo();
			final byte[][] out = new byte[splitInfos.size()][];
			for (int i = 0; i < out.length; i++) {
				final TextureSplitInfo info = splitInfos.get(i);
				out[i] = new byte[imageWidth * imageHeight * info.getFormat().getBytesPerPixel()];
			}

			for (int idx = 0; idx < pixelCount; ++idx) {
				split(out, originalData, idx);
			}

			final List<TextureImage> images = new ArrayList<>(out.length);
			for (int i = 0; i < out.length; i++) {
				final TextureSplitInfo info = splitInfos.get(i);
				final byte[] imageData = out[i];
				images.add(new TextureImage(imageWidth, imageHeight, info.getFormat(), imageData));
			}

			return images;
		}

		abstract List<TextureSplitInfo> getSplitInfo();

		abstract void split(byte[][] out, byte[] originalData, int idx);
	}

	private static final class SplitterJPGType1 extends Splitter {
		private static final List<TextureSplitInfo> channels;
		static {
			final List<TextureSplitInfo> tmp = new ArrayList<>(2);
			tmp.add(new TextureSplitInfo(TextureImageFormat.ARGB, TextureImageExpectedUseType.NORMAL));
			tmp.add(new TextureSplitInfo(TextureImageFormat.GRAYSCALE, TextureImageExpectedUseType.METALLIC));
			tmp.add(new TextureSplitInfo(TextureImageFormat.GRAYSCALE, TextureImageExpectedUseType.EMISSION));
			channels = Collections.unmodifiableList(tmp);
		}

		@Override
		public List<TextureSplitInfo> getSplitInfo() {
			return channels;
		}

		@Override
		protected void split(byte[][] out, byte[] originalData, int idx) {
			final byte normalX = originalData[idx * 4 + 0];
			final byte normalY = originalData[idx * 4 + 1];
			final byte metallic = originalData[idx * 4 + 2];
			final byte emission = originalData[idx * 4 + 3];

			final float x = (normalX & 0xFF) / 128f - 1;
			final float y = (normalY & 0xFF) / 128f - 1;
			final float z = (float) Math.sqrt(1 - x * x - y * y);
			final int normalZ = Math.round(z * 255);

			out[0][idx * 4 + 0] = (byte) 0xFF;
			out[0][idx * 4 + 1] = normalX;
			out[0][idx * 4 + 2] = normalY;
			out[0][idx * 4 + 3] = (byte) Math.max(0, Math.min(0xFF, normalZ));

			out[1][idx] = metallic;

			out[2][idx] = emission;
		}
	}

	private static final class SplitterJPGType0And2 extends Splitter {
		private static final List<TextureSplitInfo> channels;
		static {
			final List<TextureSplitInfo> tmp = new ArrayList<>(2);
			tmp.add(new TextureSplitInfo(TextureImageFormat.ARGB, TextureImageExpectedUseType.DIFFUSE));
			tmp.add(new TextureSplitInfo(TextureImageFormat.GRAYSCALE, TextureImageExpectedUseType.ROUGHNESS));
			channels = Collections.unmodifiableList(tmp);
		}

		@Override
		public List<TextureSplitInfo> getSplitInfo() {
			return channels;
		}

		@Override
		protected void split(byte[][] out, byte[] originalData, int idx) {
			final byte roughness = originalData[idx * 4 + 0];
			final byte diffuseR = originalData[idx * 4 + 1];
			final byte diffuseG = originalData[idx * 4 + 2];
			final byte diffuseB = originalData[idx * 4 + 3];

			out[0][idx * 4 + 0] = (byte) 0xFF;
			out[0][idx * 4 + 1] = diffuseR;
			out[0][idx * 4 + 2] = diffuseG;
			out[0][idx * 4 + 3] = diffuseB;

			out[1][idx] = roughness;
		}

	}

	private static final Splitter jpgType1 = new SplitterJPGType1();
	private static final Splitter jpgType0And2 = new SplitterJPGType0And2();

	/**
	 * @param header
	 *            header which belongs to the image
	 * @return true if the image is splitable
	 * @see TextureImageSplitter#split(TextureImage, TexType)
	 */
	public boolean isSplitable(StructTextureFileHeader header) {
		return isSplitable(TexType.resolve(header));
	}

	/**
	 * @param obj
	 *            which should be checked
	 * @return true if the images returned by the texture object is splitable
	 * @see TextureImageSplitter#split(TextureImage, TexType)
	 */
	public boolean isSplitable(TextureObject obj) {
		return isSplitable(obj.getTextureDataType());
	}

	/**
	 * @param texType
	 *            texType which belongs to the texture object a image is created from
	 * @return true if the images returned by the texture object is splitable
	 * @see TextureImageSplitter#split(TextureImage, TexType)
	 */
	public boolean isSplitable(TexType texType) {
		switch (texType) {
			case JPEG_TYPE_1:
			case JPEG_TYPE_2:
			case JPEG_TYPE_3:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Splits an image into its core components. Some textures are composed of different types of images. It is possible, that the components are not uniform
	 * and depend actually on some kind of material property / shader. If this is the case, this function will become deprecated and will replaced with
	 * something more appropriated.
	 *
	 * @param image
	 *            to split
	 * @param texType
	 *            type which belongs to the extracted image
	 * @return A list containing its components. List will be empty for unsplittable images.
	 */
	public List<TextureImage> split(TextureImage image, TexType texType) {
		switch (texType) {
			case JPEG_TYPE_1:
			case JPEG_TYPE_3:
				return jpgType0And2.split(image);
			case JPEG_TYPE_2:
				return jpgType1.split(image);
			default:
				return Collections.emptyList();
		}
	}

}
