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

package nexusvault.format.m3;

import java.nio.ByteOrder;
import java.util.List;

// TODO In work
public interface VertexReader {

	public static interface ModelVertexField {
		VertexField type();

		int position();

		int length();
	}

	List<ModelVertexField> getFields();

	int getVertexSizeInBytes();

	int getVertexCount();

	// boolean nextVertex();

	// boolean previousVertex();

	void moveToVertex(int index);

	int[] readFieldInt(ModelVertexField field, int[] store, int offset);

	float[] readFieldFloat(ModelVertexField field, float[] store, int offset);

	// byte[] readFieldByte(ModelVertexField field, byte[] store, int offset);

	byte[] readVertex(byte[] store, int offset);

	byte[] readVertex(byte[] store, int offset, List<ModelVertexField> fields);

	ByteOrder getByteOrder();

	Vertex readVertex();
}
