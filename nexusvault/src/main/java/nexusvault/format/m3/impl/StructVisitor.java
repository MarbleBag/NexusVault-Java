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

package nexusvault.format.m3.impl;

import nexusvault.format.m3.pointer.ArrayTypePointer;
import nexusvault.format.m3.pointer.DoubleArrayTypePointer;

public interface StructVisitor {

	void process(BytePositionTracker fileReader, int dataPosition, ArrayTypePointer<?> pointer);

	void process(BytePositionTracker fileReader, int dataPosition, DoubleArrayTypePointer<?, ?> pointer);

}
