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

package nexusvault.format.m3.impl;

import nexusvault.format.m3.TextureReference;
import nexusvault.format.m3.struct.StructTexture;

/**
 * Internal implementation. May change without notice.
 */
public final class InMemoryTextureReference implements TextureReference {

	private final StructTexture texture;
	private final InMemoryModel model;

	private String name;

	public InMemoryTextureReference(StructTexture texture, InMemoryModel model) {
		super();
		this.texture = texture;
		this.model = model;
	}

	@Override
	public String getTexturePath() {
		if (this.name == null) {
			this.name = this.texture.getName(this.model.getMemory());
		}
		return this.name;
	}

	@Override
	public TextureType getTextureType() {
		switch (this.texture.textureType) {
			case 0:
				return TextureType.DIFFUSE;
			case 1:
				return TextureType.NORMAL;
			case 2:
			default:
				return TextureType.UNKNOWN;
		}
	}

}
