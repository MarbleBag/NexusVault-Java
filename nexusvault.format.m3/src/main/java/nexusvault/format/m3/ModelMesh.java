package nexusvault.format.m3;

import java.util.List;

public interface ModelMesh {

	/**
	 * @return the index at which this mesh is defined on the model
	 */
	int getMeshIndex();

	int getMaterialReference();

	/**
	 * @return the number of vertices which are part of this mesh
	 */
	long getVertexCount();

	/**
	 * @return the number of indices which are part of this mesh
	 */
	long getIndexCount();

	/**
	 * @return all indices which are part of this mesh
	 */
	int[] getIndices();

	/**
	 * @return vertex at position <tt>idx</tt> of this mesh
	 */
	ModelVertex getVertex(int idx);

	/**
	 * @return all vertices which are part of this mesh
	 */
	List<ModelVertex> getVertices();

	@Deprecated
	Iterable<ModelVertex> iterateVertices();

}
