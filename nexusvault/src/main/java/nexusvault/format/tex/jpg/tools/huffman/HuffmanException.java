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

package nexusvault.format.tex.jpg.tools.huffman;

public final class HuffmanException extends RuntimeException {

	private static final long serialVersionUID = 6156097213710271118L;

	public HuffmanException() {
		super();
	}

	public HuffmanException(String type, String errorMessage) {
		super(type + " : " + errorMessage);
	}

	public HuffmanException(String s) {
		super(s);
	}

	public HuffmanException(String message, Throwable cause) {
		super(message, cause);
	}

	public HuffmanException(Throwable cause) {
		super(cause);
	}

}
