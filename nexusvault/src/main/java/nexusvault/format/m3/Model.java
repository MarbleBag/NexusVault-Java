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

public interface Model {

	public static Model read(byte[] data) {
		return ModelReader.read(data);
	}

	Geometry getGeometry();

	List<Material> getMaterials();

	Material getMaterial(int idx);

	List<TextureReference> getTextures();

	TextureReference getTextures(int idx);

	List<Bone> getBones();

	Bone getBone(int idx);

	int[] getBoneLookUp();

}
