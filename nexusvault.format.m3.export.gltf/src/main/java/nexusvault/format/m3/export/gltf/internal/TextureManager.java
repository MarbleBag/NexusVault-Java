package nexusvault.format.m3.export.gltf.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import de.javagl.jgltf.impl.v2.GlTF;
import nexusvault.format.m3.export.gltf.GlTFExportMonitor;
import nexusvault.format.m3.export.gltf.OnTextureRequestException;
import nexusvault.format.m3.export.gltf.ResourceBundle;

public final class TextureManager {

	private final Path outputDir;
	private final Path textureDir;
	private final GlTF gltfModel;
	private final GlTFExportMonitor monitor;

	private final Map<String, int[]> textureLookup = new HashMap<>();

	public TextureManager(Path outputDir, GlTF gltfModel, GlTFExportMonitor monitor) throws IOException {
		this.gltfModel = gltfModel;
		this.monitor = monitor;
		this.outputDir = outputDir;
		this.textureDir = outputDir.relativize(outputDir.resolve("textures"));

		Files.createDirectories(getTextureDir());
		if (!Files.isDirectory(getTextureDir())) {
			throw new IOException("Unable to create texture directory '" + getTextureDir() + "'");
		}
	}

	public Path getTextureDir() {
		return this.outputDir.resolve(this.textureDir);
	}

	private String getTextureName(String textureId) {
		if (textureId.lastIndexOf('\\') >= 0) {
			textureId = textureId.substring(textureId.lastIndexOf('\\') + 1);
		}
		return textureId;
	}

	private int[] loadTextureFromExternal(String textureId) throws IOException {
		if (textureId == null || textureId.isEmpty()) {
			return new int[0];
		}

		final var textureName = getTextureName(textureId);

		final var resourceBundle = requestTextureResources(textureId);
		final var textureResources = resourceBundle.getTextureResources();
		final var textureIndices = new int[textureResources.size()];

		for (var i = 0; i < textureResources.size(); ++i) {
			final var textureResource = textureResources.get(i);

			var imageLocation = textureResource.writeImageTo(getTextureDir());

			try {
				if (imageLocation.isAbsolute()) {
					this.monitor.newFileCreated(imageLocation);
					imageLocation = this.outputDir.relativize(imageLocation);
				} else {
					this.monitor.newFileCreated(getTextureDir().resolve(imageLocation));
				}
			} catch (final Exception e) {

			}

			final var imageIdx = GltfHelper.addImage(this.gltfModel, imageLocation);
			final var textureIdx = GltfHelper.addTexture(this.gltfModel, imageIdx, textureName);
			textureIndices[i] = textureIdx;
		}
		return textureIndices;
	}

	private ResourceBundle requestTextureResources(String textureId) {
		final var resourceBundle = new ResourceBundle();
		try {
			this.monitor.requestTexture(textureId, resourceBundle);
		} catch (final Exception e) {
			throw new OnTextureRequestException(e);
		}
		return resourceBundle;
	}

	public int[] loadTextureIntoModel(String textureId) throws IOException {
		if (this.textureLookup.containsKey(textureId)) {
			return this.textureLookup.get(textureId);
		}
		final var textureIndices = loadTextureFromExternal(textureId);
		this.textureLookup.put(textureId, textureIndices);
		return textureIndices;
	}

	public int[] getGltfTextureIndices(String textureId) {
		if (this.textureLookup.containsKey(textureId)) {
			return this.textureLookup.get(textureId);
		} else {
			return new int[0];
		}
	}

}