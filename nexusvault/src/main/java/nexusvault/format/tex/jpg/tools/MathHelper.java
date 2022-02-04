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

package nexusvault.format.tex.jpg.tools;

public final class MathHelper {
	private MathHelper() {
	}

	public static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public static int sqrtInteger(int x) {
		if (x == 0) {
			return 0;
		}
		final int low = 2 * sqrtInteger(x / 4);
		final int high = low + 1;
		if (x < high * high) {
			return low;
		}
		return high;
	}

}
