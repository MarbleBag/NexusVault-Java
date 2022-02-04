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

package nexusvault.export.m3.gltf;

import java.nio.file.Path;

public interface GlTFExportMonitor {
	/**
	 * TODO<br>
	 * <b>WIP</b><br>
	 * Up on learning more about m3 materials, this method may change to encompass this
	 *
	 * @param textureId
	 *            path of the requested id
	 * @param resourceBundle
	 *            the bundle in which the texture should be stored
	 */
	void requestTexture(String textureId, ResourceBundle resourceBundle);

	void newFileCreated(Path path);
}