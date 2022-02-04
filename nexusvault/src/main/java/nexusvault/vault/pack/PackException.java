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

package nexusvault.vault.pack;

import nexusvault.vault.VaultException;

public abstract class PackException extends VaultException {

	/**
	 * Indicates that a pack is malformed
	 */
	public static class PackMalformedException extends PackException {

		private static final long serialVersionUID = 1L;

		public PackMalformedException() {
			super();
		}

		public PackMalformedException(String s) {
			super(s);
		}

		public PackMalformedException(String message, Throwable cause) {
			super(message, cause);
		}

		public PackMalformedException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * Indicates that two different entries point to the same memory area
	 */
	public static final class PackIndexCollisionException extends PackMalformedException {

		private static final long serialVersionUID = 1L;

		public final int firstIndex;
		public final int lastIndex;
		public final long offset;

		public PackIndexCollisionException(int first, int last, long offset) {
			super(String.format("Index collision of %d and %d at %d", first, last, offset));
			this.firstIndex = first;
			this.lastIndex = last;
			this.offset = offset;
		}

	}

	public static final class PackIndexInvalidException extends PackException {

		private static final long serialVersionUID = 1L;
		public final long index;

		public PackIndexInvalidException(long index) {
			super("index: " + index);
			this.index = index;
		}

		public PackIndexInvalidException(long index, String msg) {
			super(String.format("index: %s - %s", index, msg));
			this.index = index;
		}

	}

	private static final long serialVersionUID = 1L;

	public PackException() {
		super();
	}

	public PackException(String s) {
		super(s);
	}

	public PackException(String message, Throwable cause) {
		super(message, cause);
	}

	public PackException(Throwable cause) {
		super(cause);
	}

}