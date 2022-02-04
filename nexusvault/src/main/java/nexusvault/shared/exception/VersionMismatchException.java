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

package nexusvault.shared.exception;

public class VersionMismatchException extends NexusVaultException {

	private static final long serialVersionUID = 5457680416401107519L;

	public VersionMismatchException() {
		super();
	}

	public VersionMismatchException(String s) {
		super(s);
	}

	public VersionMismatchException(String name, int expected, int actual) {
		this(name, String.valueOf(expected), String.valueOf(actual));
	}

	public VersionMismatchException(String name, String expected, String actual) {
		super(String.format("%s : Expected '%s', but was '%s'", name, expected, actual));
	}

	public VersionMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public VersionMismatchException(Throwable cause) {
		super(cause);
	}

}
