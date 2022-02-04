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

public interface Bone {

	int getBoneIndex();

	/**
	 * @return x - absolute location
	 */
	float getLocationX();

	/**
	 * @return y - absolute location
	 */
	float getLocationY();

	/**
	 * @return z - absolute location
	 */
	float getLocationZ();

	boolean hasParentBone();

	/**
	 * This method may return <code>-1</code> in case {@link #hasParentBone()} returned <code>false</code>.
	 *
	 * @return index to find its parent bone in {@link Model#getBones()}
	 */
	int getParentBoneReference();

	/**
	 * Column major transformation matrix (4x4)
	 *
	 * @return transformation matrix
	 */
	float[] getTransformationMatrix();

	/**
	 * Column major inverse transformation matrix (4x4)
	 *
	 * @return inverse transformation matrix
	 */
	float[] getInverseTransformationMatrix();

}
