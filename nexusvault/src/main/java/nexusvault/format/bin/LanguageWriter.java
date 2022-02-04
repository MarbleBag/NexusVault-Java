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

package nexusvault.format.bin;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.HashSet;

import kreed.io.util.BinaryWriter;
import kreed.io.util.ByteAlignmentUtil;
import kreed.io.util.ByteArrayBinaryWriter;
import kreed.io.util.Seek;
import kreed.io.util.SeekableByteChannelBinaryWriter;
import nexusvault.format.bin.struct.StructEntry;
import nexusvault.format.bin.struct.StructFileHeader;
import nexusvault.shared.Text;
import nexusvault.shared.exception.IntegerOverflowException;

public final class LanguageWriter {
	private LanguageWriter() {
	}

	public static byte[] toBinary(LanguageDictionary dictionary) {
		final var size = computeSize(dictionary);
		final var buffer = new byte[(int) size];
		write(dictionary, new ByteArrayBinaryWriter(buffer, ByteOrder.LITTLE_ENDIAN));
		return buffer;
	}

	public static long computeSize(LanguageDictionary dictionary) {
		long size = StructFileHeader.SIZE_IN_BYTES;
		size = ByteAlignmentUtil.alignTo16Byte(size + dictionary.locale.tagName.length() + 1);
		size = ByteAlignmentUtil.alignTo16Byte(size + dictionary.locale.shortName.length() + 1);
		size = ByteAlignmentUtil.alignTo16Byte(size + dictionary.locale.longName.length() + 1);
		size = ByteAlignmentUtil.alignTo16Byte(size + dictionary.entries.size() * StructEntry.SIZE_IN_BYTES);
		final var stringCache = new HashSet<String>();
		for (final var entry : dictionary.entries.entrySet()) {
			if (stringCache.add(entry.getValue())) {
				size += entry.getValue().length() * 2 + 2;
			}
		}
		return ByteAlignmentUtil.alignTo16Byte(size);
	}

	public static void write(LanguageDictionary dictionary, SeekableByteChannel out) {
		write(dictionary, new SeekableByteChannelBinaryWriter(out, ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN)));
	}

	public static void write(LanguageDictionary dictionary, BinaryWriter writer) {
		final long writeStartPosition = writer.position();
		writer.seek(Seek.CURRENT, StructFileHeader.SIZE_IN_BYTES);
		final long writePostHeaderPosition = writer.position();

		final var header = new StructFileHeader();
		header.signature = StructFileHeader.SIGNATURE;
		header.version = 4;
		header.languageType = dictionary.locale.type;

		header.languageTagNameOffset = writer.position() - writePostHeaderPosition;
		header.languageTagNameLength = dictionary.locale.tagName.length() + 1;
		Text.writeNullTerminatedUTF16(writer, dictionary.locale.tagName);
		ByteAlignmentUtil.alignTo16Byte(writer);

		header.languageShortNameOffset = writer.position() - writePostHeaderPosition;
		header.languageShortNameLength = dictionary.locale.shortName.length() + 1;
		Text.writeNullTerminatedUTF16(writer, dictionary.locale.shortName);
		ByteAlignmentUtil.alignTo16Byte(writer);

		header.languageLongNameOffset = writer.position() - writePostHeaderPosition;
		header.languageLongNameLength = dictionary.locale.longName.length() + 1;
		Text.writeNullTerminatedUTF16(writer, dictionary.locale.longName);
		ByteAlignmentUtil.alignTo16Byte(writer);

		{
			header.entryOffset = writer.position() - writePostHeaderPosition;
			header.entryCount = dictionary.entries.size();
			header.textOffset = ByteAlignmentUtil.alignTo16Byte(header.entryOffset + header.entryCount * StructEntry.SIZE_IN_BYTES);
			var offsetInCharacters = 0;
			final var cache = new HashMap<String, Integer>();
			for (final var entry : dictionary.entries.entrySet()) {
				final var key = entry.getKey();
				final var text = dictionary.entries.get(key);

				if (key > Math.pow(2, 32) - 1) {
					throw new IntegerOverflowException(".bin format only supports up to 2^32-1 entries");
				}

				writer.writeInt32(key);
				if (cache.containsKey(text)) {
					writer.writeInt32(cache.get(text).intValue());
				} else {
					writer.writeInt32(offsetInCharacters);
					cache.put(text, Integer.valueOf(offsetInCharacters));

					final var position = writer.position();
					writer.seek(Seek.BEGIN, writePostHeaderPosition + header.textOffset + offsetInCharacters * 2);
					offsetInCharacters += Text.writeNullTerminatedUTF16(writer, text) / 2;
					writer.seek(Seek.BEGIN, position);
				}
			}
		}

		{
			final var writeEndOfFile = ByteAlignmentUtil.alignTo16Byte(writer.position());
			final var padding = writeEndOfFile - writer.position();
			for (var i = 0; i < padding; ++i) {
				writer.writeInt8(0);
			}
		}

		writer.seek(Seek.BEGIN, writeStartPosition);
		header.write(writer);
	}

}
