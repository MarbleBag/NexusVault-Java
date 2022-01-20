package nexusvault.export.m3.gltf;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public final class ResourceBundle {

	private final List<TextureResource> resources = new LinkedList<>();

	/**
	 * See {@link TextureResource} for a list of possible resource types
	 * 
	 * @param resource
	 *            a list of possible resources
	 */
	public void addTextureResource(List<TextureResource> resource) {
		if (resource == null) {
			return;
		}
		resources.addAll(resource);
	}

	/**
	 * See {@link TextureResource} for a list of possible resource types
	 * 
	 * @param resource
	 *            a possible resource
	 */
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