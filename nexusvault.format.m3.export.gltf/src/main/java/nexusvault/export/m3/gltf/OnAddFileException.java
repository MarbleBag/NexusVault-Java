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

import nexusvault.shared.exception.ExportException;

public final class OnAddFileException extends ExportException {

	private static final long serialVersionUID = -2047895574718079791L;

	public OnAddFileException() {
		super();
	}

	public OnAddFileException(String s) {
		super(s);
	}

	public OnAddFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public OnAddFileException(Throwable cause) {
		super(cause);
	}

}
