package nexusvault.format.m3;

import java.util.List;

public interface ModelGeometry {

	/**
	 * @return the number of meshes this model has
	 */
	int getMeshCount();

	/**
	 * @return the number of all vertices this model has
	 */
	long getVertexCount();

	/**
	 * @return the number of all indices this model has
	 */
	long getIndexCount();

	List<ModelMesh> getMeshes();

	ModelMesh getMesh(int idx);

	boolean hasVertexLocation();

	boolean hasVertex1TextureCoords();

	boolean hasVertex2TextureCoords();

	boolean hasVertexBoneIndices();

	boolean hasVertexBoneWeights();

}
