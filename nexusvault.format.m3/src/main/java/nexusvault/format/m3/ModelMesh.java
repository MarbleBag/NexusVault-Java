package nexusvault.format.m3;

import java.util.List;

public interface ModelMesh {

	/**
	 * @return the index at which this mesh is defined on the model
	 */
	int getMeshIndex();

	int getMaterialReference();

	boolean hasMeshGroup();

	int getMeshGroup();

	/**
	 * A mesh can be assigned to a specific part of a body. The meaning of some of its values can be looked up in {@link MeshToBodyPart}
	 * <p>
	 * It seems this value is only used in models of playable races.
	 * 
	 * @return the index of assigned body part
	 * @see MeshToBodyPart
	 */
	int getMeshToBodyPart();

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
	 * @param idx
	 *            index of the vertex which should be returned
	 * @return vertex at position <code>idx</code> of this mesh
	 * @throws IndexOutOfBoundsException
	 *             if <code>idx</code> &lt; 0 OR {@link #getIndexCount()} &lt;= <code>idx</code>
	 */
	ModelVertex getVertex(int idx);

	/**
	 * @return all vertices which are part of this mesh
	 */
	List<ModelVertex> getVertices();

	@Deprecated
	Iterable<ModelVertex> iterateVertices();

}
