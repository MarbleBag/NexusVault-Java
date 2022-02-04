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

package nexusvault.vault;

import nexusvault.shared.exception.NexusVaultException;

/**
 * Base exception related to all exceptions thrown
 */
public class VaultException extends NexusVaultException {

	private static final long serialVersionUID = 1L;

	public VaultException() {
		super();
	}

	public VaultException(String s) {
		super(s);
	}

	public VaultException(String message, Throwable cause) {
		super(message, cause);
	}

	public VaultException(Throwable cause) {
		super(cause);
	}

}
