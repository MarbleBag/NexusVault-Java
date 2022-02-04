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

package nexusvault.vault.codec;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import kreed.io.util.BinaryReader;
import nexusvault.shared.exception.IntegerOverflowException;

public final class ZipCodec {
	private ZipCodec() {
	}

	public static byte[] decode(byte[] data, long uncompressedSize) throws DecodeException {
		if (uncompressedSize < 0 || uncompressedSize > Integer.MAX_VALUE) {
			throw new IntegerOverflowException();
		}

		try {
			final var inflater = new java.util.zip.Inflater();
			inflater.setInput(data);

			final byte[] output = new byte[(int) uncompressedSize];
			final int resultLength = inflater.inflate(output);

			inflater.end();

			if (output.length != resultLength) {
				throw new DecodeException("ZLIB: Uncompressed Size does not match expecations. Got " + resultLength + " expected " + output.length);
			}

			return output;
		} catch (final DataFormatException e) {
			throw new DecodeException(e);
		}
	}

	public static byte[] decode(BinaryReader reader, int inputLength, long uncompressedSize) throws DecodeException {
		try {
			if (uncompressedSize < 0 || uncompressedSize > Integer.MAX_VALUE) {
				throw new IntegerOverflowException();
			}

			final byte[] rawData = new byte[inputLength];
			reader.readInt8(rawData, 0, rawData.length);

			final Inflater inflater = new java.util.zip.Inflater();
			inflater.setInput(rawData);

			final byte[] output = new byte[(int) uncompressedSize];
			final int resultLength = inflater.inflate(output);

			inflater.end();

			if (output.length != resultLength) {
				throw new DecodeException("ZLIB: Uncompressed Size does not match expecations. Got " + resultLength + " expected " + output.length);
			}

			return output;
		} catch (final DataFormatException e) {
			throw new DecodeException(e);
		}
	}

	public static byte[] encode(byte[] data) throws EncodeException {
		final var deflater = new java.util.zip.Deflater();
		deflater.setInput(data);
		deflater.finish();

		final var encoded = new ByteArrayOutputStream(Math.min(data.length, 2 << 10));
		final byte[] buffer = new byte[Math.min(data.length, 2 << 10)];

		while (!deflater.finished()) {
			final int writtenBytes = deflater.deflate(buffer);
			encoded.write(buffer, 0, writtenBytes);
		}

		return encoded.toByteArray();
	}

	public static byte[] encode(ByteBuffer data) throws EncodeException {
		final var deflater = new java.util.zip.Deflater();
		deflater.setInput(data);
		deflater.finish();

		final var encoded = new ByteArrayOutputStream(Math.min(data.remaining(), 2 << 10));
		final byte[] buffer = new byte[Math.min(data.remaining(), 2 << 10)];

		while (!deflater.finished()) {
			final int writtenBytes = deflater.deflate(buffer);
			encoded.write(buffer, 0, writtenBytes);
		}

		return encoded.toByteArray();
	}

	public static byte[] encode(BinaryReader reader, int inputLength) throws EncodeException {
		final var data = new byte[inputLength];
		reader.readInt8(data, 0, data.length);
		return encode(data);
	}

}
