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

package nexusvault.export.m3.obj;

enum ObjIllum {
	/** Color on and Ambient off */
	ZERO,
	/** Color on and Ambient on */
	ONE,
	/** Highlight on */
	TWO,
	/** Reflection on and Ray trace on */
	THREE,
	/** Transparency: Glass on, Reflection: Ray trace on */
	FOUR,
	/** Reflection: Fresnel on and Ray trace on */
	FIVE,
	/** Transparency: Refraction on, Reflection: Fresnel off and Ray trace on */
	SIX,
	/** Transparency: Refraction on, Reflection: Fresnel on and Ray trace on */
	SEVEN,
	/** Reflection on and Ray trace off */
	EIGHT,
	/** Transparency: Glass on, Reflection: Ray trace off */
	NINE,
	/** Casts shadows onto invisible surfaces */
	TEN;

	public int getValue() {
		return ordinal();
	}
}