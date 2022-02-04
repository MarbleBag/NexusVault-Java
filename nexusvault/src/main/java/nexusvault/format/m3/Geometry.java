/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.m3;

import java.util.List;
import java.util.Set;

public interface Geometry {

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

	List<Mesh> getMeshes();

	Mesh getMesh(int idx);

	Set<VertexField> getAvailableVertexFields();

	boolean isVertexFieldAvailable(VertexField field);

	/**
	 * @return true if a vertex has location data
	 * @see Vertex#getLocationX()
	 * @see Vertex#getLocationY()
	 * @see Vertex#getLocationZ()
	 * @see Vertex#getLocation(float[], int)
	 */
	boolean hasVertexLocation();

	/**
	 * @return true if a vertex has only one set of texture coordinates
	 * @see Vertex#getTextureCoordU1()
	 * @see Vertex#getTextureCoordV1()
	 * @see Vertex#getTexCoords(float[], int)
	 */
	boolean hasVertex1TextureCoords();

	/**
	 * @return true if a vertex has two sets of texture coordinates
	 * @see Vertex#getTextureCoordU1()
	 * @see Vertex#getTextureCoordV1()
	 * @see Vertex#getTextureCoordU2()
	 * @see Vertex#getTextureCoordV2()
	 * @see Vertex#getTexCoords(float[], int)
	 */
	boolean hasVertex2TextureCoords();

	/**
	 * @return true if a vertex has bone indices
	 * @see Vertex#getBoneIndex1()
	 * @see Vertex#getBoneIndex2()
	 * @see Vertex#getBoneIndex3()
	 * @see Vertex#getBoneIndex4()
	 */
	boolean hasVertexBoneIndices();

	/**
	 * @return true if a vertex has bone weights
	 * @see Vertex#getBoneWeight1()
	 * @see Vertex#getBoneWeight2()
	 * @see Vertex#getBoneWeight3()
	 * @see Vertex#getBoneWeight4()
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
