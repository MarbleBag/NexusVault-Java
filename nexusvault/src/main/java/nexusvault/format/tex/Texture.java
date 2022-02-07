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

import nexusvault.format.tex.Image.ImageFormat;
import nexusvault.format.tex.dxt.DXTImageReader;
import nexusvault.format.tex.jpg.JPGImageReader;
import nexusvault.format.tex.struct.StructFileHeader;
import nexusvault.format.tex.uncompressed.PlainImageReader;

public final class Texture {

	public static Texture read(byte[] data) {
		return new Texture(data);
	}

	private final StructFileHeader header;
	private final byte[] data;

	public Texture(byte[] data) {
		this.header = TextureReader.getFileHeader(data);
		this.data = data;
	}

	public TextureType getTextureType() {
		return TextureType.resolve(this.header);
	}

	public Image.ImageFormat getImageFormat() {
		switch (getTextureType()) {
			case ARGB1:
			case ARGB2:
			case DXT1:
			case DXT3:
			case DXT5:
			case JPG1:
			case JPG2:
			case JPG3:
				return ImageFormat.ARGB;
			case RGB:
				return ImageFormat.RGB;
			case GRAYSCALE:
				return ImageFormat.GRAYSCALE;
			default:
				throw new TextureException();
		}
	}

	public Image getMipMap(int index) {
		index = this.header.textureCount - index - 1;
		switch (getTextureType()) {
			case DXT1:
			case DXT3:
			case DXT5:
				return DXTImageReader.decompress(this.header, this.data, StructFileHeader.SIZE_IN_BYTES, index);
			case JPG1:
			case JPG2:
			case JPG3:
				return JPGImageReader.decompress(this.header, this.data, StructFileHeader.SIZE_IN_BYTES, index);
			case ARGB1:
			case ARGB2:
			case RGB:
			case GRAYSCALE:
				return PlainImageReader.getImage(this.header, this.data, StructFileHeader.SIZE_IN_BYTES, index);
			default:
				throw new TextureException();
		}
	}

	public int getMipMapCount() {
		return this.header.textureCount;
	}

	public int getWidth() {
		return this.header.width;
	}

	public int getHeight() {
		return this.header.height;
	}

	public int getVersion() {
		return this.header.version;
	}

	public int getSides() {
		return this.header.sides;
	}

	public int getDepth() {
		return this.header.depth;
	}

}
