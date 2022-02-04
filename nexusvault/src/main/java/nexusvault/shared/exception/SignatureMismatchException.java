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

public class SignatureMismatchException extends NexusVaultException {

	private static final long serialVersionUID = -7254283632187562387L;

	public SignatureMismatchException() {
		super();
	}

	public SignatureMismatchException(String s) {
		super(s);
	}

	public SignatureMismatchException(String name, int expected, int actual) {
		super(String.format("%s : Expected '%s', but was '%s'", name, toString(expected), toString(actual)));
	}

	public static String toString(int signature) {
		final char a = (char) (signature >> 0x18 & 0xFF);
		final char b = (char) (signature >> 0x10 & 0xFF);
		final char c = (char) (signature >> 0x08 & 0xFF);
		final char d = (char) (signature >> 0x00 & 0xFF);
		return "" + a + b + c + d;
	}

	public SignatureMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public SignatureMismatchException(Throwable cause) {
		super(cause);
	}

}