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

package nexusvault.format.tex;

import java.nio.ByteOrder;

import kreed.io.util.BinaryReader;
import kreed.io.util.ByteArrayBinaryReader;
import nexusvault.format.tex.dxt.DXTImageReader;
import nexusvault.format.tex.jpg.JPGImageReader;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.format.tex.uncompressed.PlainImageReader;

/**
 * @see DXTImageReader
 * @see JPGImageReader
 * @see PlainImageReader
 * @see Texture
 */
public final class TextureReader {
	private TextureReader() {

	}

	public static StructFileHeader getFileHeader(byte[] data) {
		return readFileHeader(new ByteArrayBinaryReader(data, ByteOrder.LITTLE_ENDIAN));
	}

	public static StructFileHeader readFileHeader(BinaryReader reader) {
		return new StructFileHeader(reader);
	}

	public static Texture read(byte[] data) {
		return new Texture(data);
	}

	public static Image readFirstImage(byte[] data) {
		final var texture = read(data);
		return texture.getMipMap(0);
	}

}
