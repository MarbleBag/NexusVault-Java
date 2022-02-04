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

package nexusvault.vault.util;

import java.nio.file.Path;

public final class ArchivePathLocator {
	private ArchivePathLocator() {
	}

	public static Path getArchivePath(Path archiveOrIndex) {
		String fileName = archiveOrIndex.getFileName().toString();
		if (fileName.endsWith(".index")) {
			fileName = fileName.substring(0, fileName.lastIndexOf(".index")) + ".archive";
			return archiveOrIndex.resolveSibling(fileName);
		} else if (fileName.endsWith(".archive")) {
			return archiveOrIndex;
		} else {
			throw new IllegalArgumentException(String.format("Path %s neither points to an index- nor an archive-file", archiveOrIndex));
		}
	}

	public static Path getIndexPath(Path archiveOrIndex) {
		String fileName = archiveOrIndex.getFileName().toString();
		if (fileName.endsWith(".index")) {
			return archiveOrIndex;
		} else if (fileName.endsWith(".archive")) {
			fileName = fileName.substring(0, fileName.lastIndexOf(".archive")) + ".index";
			return archiveOrIndex.resolveSibling(fileName);
		} else {
			throw new IllegalArgumentException(String.format("Path %s neither points to an index- nor an archive-file", archiveOrIndex));
		}
	}
}