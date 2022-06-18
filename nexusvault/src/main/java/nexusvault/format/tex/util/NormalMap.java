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

package nexusvault.format.tex.util;

import java.util.Objects;

import nexusvault.format.tex.Image;
import nexusvault.format.tex.Image.ImageFormat;

public final class NormalMap {

	public static void compressTangentSpaceNormalMap(Image src, Image dst, int dstX, int dstY) {
		compressTangentSpaceNormalMap(src, dstX, dstY, dst, dstX, dstY);
	}

	public static void compressTangentSpaceNormalMap(Image src, int srcX, int srcY, Image dst, int dstX, int dstY) {
		Objects.requireNonNull(src, "Argument: 'src'");
		Objects.requireNonNull(dst, "Argument: 'dst'");

		if (src.getWidth() != dst.getWidth() || src.getHeight() != dst.getHeight()) {
			throw new IllegalArgumentException("Images must have same dimension");
		}

		final var srcData = src.getData();
		final var dstData = dst.getData();

		final var srcBpp = src.getFormat().getBytesPerPixel();
		final var dstBpp = dst.getFormat().getBytesPerPixel();

		for (int srcIndex = 0, dstIndex = 0; srcIndex < srcData.length; srcIndex += srcBpp, dstIndex += dstBpp) {
			dstData[dstIndex + dstX] = srcData[srcIndex + srcX];
			dstData[dstIndex + dstY] = srcData[srcIndex + srcY];
		}
	}

	public static Image decompressTangentSpaceNormalMap(Image image) {
		return decompressTangentSpaceNormalMap(image, 0, 1);
	}

	public static Image decompressTangentSpaceNormalMap(Image src, int srcX, int srcY) {
		Objects.requireNonNull(src, "Argument: 'src'");

		final var srcData = src.getData();
		final var srcBpp = src.getFormat().getBytesPerPixel();

		final var normalMap = new byte[ImageFormat.RGB.getBytesPerPixel() * src.getWidth() * src.getHeight()];
		for (int srcIndex = 0, dstIndex = 0; srcIndex < srcData.length; srcIndex += srcBpp) {
			final byte normalX = srcData[srcIndex + srcX];
			final byte normalY = srcData[srcIndex + srcY];

			final float x = (normalX & 0xFF) / 127.5f - 1;
			final float y = (normalY & 0xFF) / 127.5f - 1;
			final float z = (float) Math.sqrt(1 - x * x - y * y);
			final byte normalZ = (byte) Math.max(0, Math.min(0xFF, Math.round(z * 255)));

			normalMap[dstIndex++] = normalX;
			normalMap[dstIndex++] = normalY;
			normalMap[dstIndex++] = normalZ;
		}

		return new Image(src.getWidth(), src.getHeight(), ImageFormat.RGB, normalMap);
	}

}
