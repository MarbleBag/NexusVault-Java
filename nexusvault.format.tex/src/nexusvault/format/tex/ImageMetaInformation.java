package nexusvault.format.tex;

public class ImageMetaInformation {
	public final int offset;
	public final int length;
	public final int width;
	public final int height;

	public ImageMetaInformation(int offset, int length, int width, int height) {
		super();
		this.offset = offset;
		this.length = length;
		this.width = width;
		this.height = height;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ImageInformation [offset=");
		builder.append(offset);
		builder.append(", length=");
		builder.append(length);
		builder.append(", width=");
		builder.append(width);
		builder.append(", height=");
		builder.append(height);
		builder.append("]");
		return builder.toString();
	}

}