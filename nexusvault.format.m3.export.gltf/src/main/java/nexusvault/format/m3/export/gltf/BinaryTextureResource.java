package nexusvault.format.m3.export.gltf;

import java.nio.file.Path;

import de.javagl.jgltf.impl.v2.Image;
import kreed.io.util.BinaryReader;

// TODO
final class BinaryTextureResource extends TextureResource {
	public static enum TextureFormat {
		ARGB,
		RGB,
		GRAY,
		MIME
	}

	public static enum MimeType {
		PNG("image/png"),
		JPEG("image/jpeg");

		private final String mime;

		private MimeType(String mime) {
			this.mime = mime;
		}

		protected String getMimeType() {
			return mime;
		}
	}

	public BinaryTextureResource(BinaryReader reader, BinaryTextureResource.TextureFormat format) {
		// TODO
	}

	public BinaryTextureResource(BinaryReader reader, BinaryTextureResource.MimeType format) {
		// TODO
	}

	@Override
	Image writeImageTo(Path outputDirectory, String outputFileName) {
		// TODO Auto-generated method stub
		return null;
	}
}