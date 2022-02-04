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
		this.resources.addAll(resource);
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
		this.resources.add(resource);
	}

	public List<TextureResource> getTextureResources() {
		return this.resources;
	}

}