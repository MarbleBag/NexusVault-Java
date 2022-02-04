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

/**
 * Thrown to indicate that a structure (created by hand or more so created by parsing a WS related file) has data that deviates from the expectation and needs
 * to be reviewed.
 */
public class StructException extends NexusVaultException {

	/**
	 *
	 */
	private static final long serialVersionUID = -6162209104779255483L;

	public StructException() {
		super();
	}

	public StructException(String s) {
		super(s);
	}

	public StructException(String message, Throwable cause) {
		super(message, cause);
	}

	public StructException(Throwable cause) {
		super(cause);
	}

}
