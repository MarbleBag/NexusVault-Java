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
 * Base exception for all NexusVault related exceptions.
 */
public class NexusVaultException extends RuntimeException {

	private static final long serialVersionUID = 5287804699640781565L;

	public NexusVaultException() {
		super();
	}

	public NexusVaultException(String s) {
		super(s);
	}

	public NexusVaultException(String message, Throwable cause) {
		super(message, cause);
	}

	public NexusVaultException(Throwable cause) {
		super(cause);
	}

}
