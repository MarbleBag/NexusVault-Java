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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// TODO
public final class PathTextureResource extends TextureResource {
	private final Path source;

	public PathTextureResource(Path path) {
		if (path == null) {
			throw new IllegalArgumentException("'path' must not be null");
		}
		this.source = path;
	}

	@Override
	public Path writeImageTo(Path outputDirectory) throws IOException {
		final var dst = outputDirectory.resolve(this.source.getFileName());
		if (this.source.equals(dst)) {
			return dst;
		}
		Files.copy(this.source, dst, StandardCopyOption.REPLACE_EXISTING);
		return dst;
	}
}