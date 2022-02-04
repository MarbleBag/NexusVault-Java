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

package nexusvault.vault.archive;

import nexusvault.vault.VaultException;

public abstract class ArchiveException extends VaultException {

	public static final class ArchiveHashCollisionException extends ArchiveException {

		private static final long serialVersionUID = 1L;

		public ArchiveHashCollisionException(String s) {
			super(s);
		}

		public ArchiveHashCollisionException() {
			super();
		}

		public ArchiveHashCollisionException(String message, Throwable cause) {
			super(message, cause);
		}

		public ArchiveHashCollisionException(Throwable cause) {
			super(cause);
		}

	}

	public static final class ArchiveHashException extends ArchiveException {

		private static final long serialVersionUID = 1L;

		public ArchiveHashException() {
			super();
		}

		public ArchiveHashException(String s) {
			super(s);
		}

		public ArchiveHashException(String message, Throwable cause) {
			super(message, cause);
		}

		public ArchiveHashException(Throwable cause) {
			super(cause);
		}

	}

	public static final class ArchiveHashNotFoundException extends ArchiveException {

		private static final long serialVersionUID = 1L;

		public ArchiveHashNotFoundException() {
			super();
		}

		public ArchiveHashNotFoundException(String s) {
			super(s);
		}

		public ArchiveHashNotFoundException(String message, Throwable cause) {
			super(message, cause);
		}

		public ArchiveHashNotFoundException(Throwable cause) {
			super(cause);
		}

	}

	private static final long serialVersionUID = 1L;

	public ArchiveException() {
		super();
	}

	public ArchiveException(String s) {
		super(s);
	}

	public ArchiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArchiveException(Throwable cause) {
		super(cause);
	}

}
