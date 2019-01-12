package nexusvault.format.m3.export.gltf;

import java.util.LinkedList;
import java.util.List;

public final class ResourceBundle {

	private final List<TextureResource> resources = new LinkedList<>();

	public void addTextureResource(List<TextureResource> resource) {
		if (resource == null) {
			return;
		}
		resources.addAll(resource);
	}

	public void addTextureResource(TextureResource resource) {
		if (resource == null) {
			return;
		}
		resources.add(resource);
	}

	public List<TextureResource> getTextureResources() {
		return resources;
	}

}