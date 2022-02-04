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

public final class HexToString {

	public static String byteToHex(byte[] arr) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : arr) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	public static String toBinary(int i) {
		return String.format("%32s", Integer.toBinaryString(i)).replace(" ", "0");
	}

	public static String toBinary(short i) {
		return String.format("%16s", Integer.toBinaryString(i & 0xFFFF)).replace(" ", "0");
	}

	public static String toBinary(byte i) {
		return String.format("%8s", Integer.toBinaryString(i & 0xFF)).replace(" ", "0");
	}

	public static String toBinary(byte[] i) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : i) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(toBinary(b));
		}
		return sb.toString();
	}

}
