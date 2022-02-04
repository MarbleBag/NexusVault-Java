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

package nexusvault.vault.index;

import nexusvault.vault.VaultException;

public abstract class IndexException extends VaultException {

	public static final class IndexEntryNotFoundException extends IndexException {
		private static final long serialVersionUID = 1L;

		public IndexEntryNotFoundException(String s) {
			super(s);
		}
	}

	public static final class IndexEntryNotAFileException extends IndexException {
		private static final long serialVersionUID = 1L;

		public IndexEntryNotAFileException(String s) {
			super(s);
		}
	}

	public static final class IndexEntryNotADirectoryException extends IndexException {

		private static final long serialVersionUID = 1L;

		public IndexEntryNotADirectoryException(String s) {
			super(s);
		}
	}

	public static final class IndexNameCollisionException extends IndexException {

		private static final long serialVersionUID = 1L;

		public IndexNameCollisionException(String s) {
			super(s);
		}
	}

	private static final long serialVersionUID = 1L;

	public IndexException() {
		super();
	}

	public IndexException(String s) {
		super(s);
	}

	public IndexException(String message, Throwable cause) {
		super(message, cause);
	}

	public IndexException(Throwable cause) {
		super(cause);
	}

}
