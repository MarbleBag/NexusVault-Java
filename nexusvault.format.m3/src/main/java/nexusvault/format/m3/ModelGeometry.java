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

	/**
	 * @return true if a vertex has location data
	 * @see ModelVertex#getLocationX()
	 * @see ModelVertex#getLocationY()
	 * @see ModelVertex#getLocationZ()
	 * @see ModelVertex#getLocation(float[], int)
	 */
	boolean hasVertexLocation();

	/**
	 * @return true if a vertex has only one set of texture coordinates
	 * @see ModelVertex#getTextureCoordU1()
	 * @see ModelVertex#getTextureCoordV1()
	 * @see ModelVertex#getTexCoords(float[], int)
	 */
	boolean hasVertex1TextureCoords();

	/**
	 * @return true if a vertex has two sets of texture coordinates
	 * @see ModelVertex#getTextureCoordU1()
	 * @see ModelVertex#getTextureCoordV1()
	 * @see ModelVertex#getTextureCoordU2()
	 * @see ModelVertex#getTextureCoordV2()
	 * @see ModelVertex#getTexCoords(float[], int)
	 */
	boolean hasVertex2TextureCoords();

	/**
	 * @return true if a vertex has bone indices
	 * @see ModelVertex#getBoneIndex1()
	 * @see ModelVertex#getBoneIndex2()
	 * @see ModelVertex#getBoneIndex3()
	 * @see ModelVertex#getBoneIndex4()
	 */
	boolean hasVertexBoneIndices();

	/**
	 * @return true if a vertex has bone weights
	 * @see ModelVertex#getBoneWeight1()
	 * @see ModelVertex#getBoneWeight2()
	 * @see ModelVertex#getBoneWeight3()
	 * @see ModelVertex#getBoneWeight4()
	 */
	boolean hasVertexBoneWeights();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known.
	 *
	 * @return true if the vertex has unknown data of type 1
	 */
	boolean hasVertexUnknownData1();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known.
	 *
	 * @return true if the vertex has unknown data of type 2
	 */
	boolean hasVertexUnknownData2();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known.
	 *
	 * @return true if the vertex has unknown data of type 3
	 */
	boolean hasVertexUnknownData3();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known.
	 *
	 * @return true if the vertex has unknown data of type 4
	 */
	boolean hasVertexUnknownData4();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known.
	 *
	 * @return true if the vertex has unknown data of type 5
	 */
	boolean hasVertexUnknownData5();

	/**
	 * <b>Note:</b> This method will change in the future, when the purpose of this unknown vertex information is known.
	 *
	 * @return true if the vertex has unknown data of type 6
	 */
	boolean hasVertexUnknownData6();

}
