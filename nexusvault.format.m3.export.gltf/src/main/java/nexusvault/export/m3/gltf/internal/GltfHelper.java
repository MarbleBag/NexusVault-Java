package nexusvault.export.m3.gltf.internal;

import java.nio.file.Path;
import java.util.Collection;

import de.javagl.jgltf.impl.v2.Accessor;
import de.javagl.jgltf.impl.v2.Asset;
import de.javagl.jgltf.impl.v2.Buffer;
import de.javagl.jgltf.impl.v2.BufferView;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.impl.v2.Image;
import de.javagl.jgltf.impl.v2.Mesh;
import de.javagl.jgltf.impl.v2.Node;
import de.javagl.jgltf.impl.v2.Scene;
import de.javagl.jgltf.impl.v2.Texture;
import nexusvault.export.m3.gltf.internal.vertex.VertexFieldWriter;

public final class GltfHelper {
	private GltfHelper() {
	}

	public static GlTF createBaseModel() {
		final var gltfModel = new GlTF();

		final var asset = new Asset();
		asset.setVersion("2.0");
		asset.setGenerator("nexusvault-glTF-exporter");
		gltfModel.setAsset(asset);

		final var defaultScene = new Scene();
		defaultScene.addNodes(0); // scene starts with node at index 0

		gltfModel.addScenes(defaultScene);
		gltfModel.setScene(0); // scene to show

		final var root = new Node();
		gltfModel.addNodes(root); // node 0

		return gltfModel;
	}

	public static int getSize(Collection collection) {
		return collection == null ? 0 : collection.size();
	}

	public static int addBuffer(GlTF model, String uri, int size) {
		final var buffer = new Buffer();
		buffer.setByteLength(size);
		buffer.setUri(uri);
		model.addBuffers(buffer);
		return getBufferCount(model) - 1;
	}

	public static void updateBuffer(GlTF model, int idx, int writtenBytes) {
		final var buffer = model.getBuffers().get(idx);
		final var currentSize = buffer.getByteLength();
		final var newSize = currentSize + writtenBytes;
		buffer.setByteLength(newSize);
	}

	public static int getBufferCount(GlTF model) {
		return getSize(model.getBuffers());
	}

	public static int addBufferView(GlTF model, int offset, int length) {
		final var view = new BufferView();
		model.addBufferViews(view);

		view.setBuffer(0); // default buffer
		view.setByteOffset(offset);
		view.setByteLength(length);

		return getBufferViewCount(model) - 1;
	}

	public static int getBufferViewCount(GlTF model) {
		return getSize(model.getBufferViews());
	}

	public static int addAccessor(GlTF model, GlTFType type, GlTFComponentType componentType) {
		final var accessor = new Accessor();
		model.addAccessors(accessor);
		accessor.setType(type.getId());
		accessor.setComponentType(componentType.getId());

		return getAccessorCount(model) - 1;
	}

	public static int addAccessor(GlTF model, VertexFieldWriter field, int vertexCount) {
		final var idx = addAccessor(model, field.getType(), field.getComponentType());
		final var accessor = model.getAccessors().get(idx);

		accessor.setByteOffset(field.getFieldOffset());
		accessor.setCount(vertexCount);
		if (field.hasMinimum()) {
			accessor.setMin(field.getMinimum());
		}
		if (field.hasMaximum()) {
			accessor.setMax(field.getMaximum());
		}

		return idx;
	}

	public static int getAccessorCount(GlTF model) {
		return getSize(model.getAccessors());
	}

	public static int addMesh(GlTF model) {
		final var mesh = new Mesh();
		model.addMeshes(mesh);
		return getMeshCount(model) - 1;
	}

	public static int getMeshCount(GlTF model) {
		return getSize(model.getMeshes());
	}

	public static int getNodeCount(GlTF model) {
		return getSize(model.getNodes());
	}

	public static Node getRootNode(GlTF model) {
		return model.getNodes().get(0);
	}

	public static int addImage(GlTF model, Path uri) {
		final var image = new Image();
		image.setUri(uri.toString());

		// this is has to be done because of a bug in de.javagl.jgltf.model.io.GltfModelWriter.GltfModelWriter(), which does not accept an image
		// without a bufferview, even while it's not even using one.
		image.setBufferView(0);

		model.addImages(image);
		return getImageCount(model) - 1;
	}

	public static int getImageCount(GlTF model) {
		return getSize(model.getImages());
	}

	public static int addTexture(GlTF model, int srcIdx, String name) {
		final var texture = new Texture();
		texture.setSource(srcIdx);
		texture.setName(name);
		model.addTextures(texture);
		return getTextureCount(model) - 1;
	}

	private static int getTextureCount(GlTF model) {
		return getSize(model.getTextures());
	}

}