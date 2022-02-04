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

public final class Image {

	public static enum ImageFormat {
		/** 3 bytes per pixel */
		RGB(3),
		/** 4 bytes per pixel */
		ARGB(4),
		/** 1 bytes per pixel */
		GRAYSCALE(1);

		private final int bytePerPixel;

		private ImageFormat(int bytePerPixel) {
			this.bytePerPixel = bytePerPixel;
		}

		public int getBytesPerPixel() {
			return this.bytePerPixel;
		}
	}

	private final ImageFormat format;
	private final int width;
	private final int height;
	private final byte[] data;

	public Image(int width, int height, ImageFormat format, byte[] data) {
		if (width <= 0) {
			throw new IllegalArgumentException("'width' must be greater than zero");
		}
		if (height <= 0) {
			throw new IllegalArgumentException("'height' must be greater than zero");
		}
		if (format == null) {
			throw new IllegalArgumentException("'format' must not be null");
		}
		if (data == null) {
			throw new IllegalArgumentException("'data' must not be null");
		}

		this.width = width;
		this.height = height;
		this.format = format;
		this.data = data;

		final int bytesPerPixel = format.getBytesPerPixel();
		final int expectedBytes = width * height * bytesPerPixel;
		if (data.length != expectedBytes) {
			throw new IllegalArgumentException(
					String.format("Image data does not fit an image of %dx%d of type %s. Expected number of bytes %d, actual number of bytes %d", width, height,
							format.name(), expectedBytes, data.length));
		}
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	public byte[] getData() {
		return this.data;
	}

	public ImageFormat getFormat() {
		return this.format;
	}

}
