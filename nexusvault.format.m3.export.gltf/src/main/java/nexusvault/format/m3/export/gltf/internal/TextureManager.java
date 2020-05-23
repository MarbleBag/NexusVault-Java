package nexusvault.format.m3.export.gltf.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import de.javagl.jgltf.impl.v2.GlTF;
import nexusvault.format.m3.export.gltf.GlTFExportMonitor;
import nexusvault.format.m3.export.gltf.OnTextureRequestException;
import nexusvault.format.m3.export.gltf.ResourceBundle;

public final class TextureManager {

	private final Path textureDirectory;
	private final GlTF gltfModel;
	private final GlTFExportMonitor monitor;

	private final Map<String, int[]> textureLookup = new HashMap<>();

	public TextureManager(Path textureDirectory, GlTF gltfModel, GlTFExportMonitor monitor) {
		this.textureDirectory = textureDirectory;
		this.gltfModel = gltfModel;
		this.monitor = monitor;
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

			final var imageLocation = textureResource.writeImageTo(this.textureDirectory);

			try {
				if (imageLocation.isAbsolute()) {
					this.monitor.newFileCreated(imageLocation);
				} else {
					this.monitor.newFileCreated(this.textureDirectory.resolve(imageLocation.subpath(1, imageLocation.getNameCount())));
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