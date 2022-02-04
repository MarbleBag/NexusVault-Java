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

package nexusvault.format.tex.jpg.tools.huffman;

public final class HuffmanDecoder {
	private HuffmanDecoder() {
	}

	public static interface BitSupply {

		boolean canSupply(int numberOfBits);

		int supply(int numberOfBits);

	}

	public static void decode(HuffmanTable dc, HuffmanTable ac, BitSupply supplier, int[] dst, int dstOffset, int dstLength) {
		if (dc == null) {
			throw new IllegalArgumentException("'dc' must not be null");
		}
		if (ac == null) {
			throw new IllegalArgumentException("'ac' must not be null");
		}
		if (supplier == null) {
			throw new IllegalArgumentException("'supplier' must not be null");
		}
		if (dst == null) {
			throw new IllegalArgumentException("'dst' must not be null");
		}

		assertNotOutOfBounds("dstOffset", dstOffset, 0, dst.length);
		assertNotOutOfBounds("dstLength", dstLength, 1, dst.length);
		assertNotOutOfBounds("dstOffset+dstLength", dstOffset + dstLength, 0, dst.length);

		final int dcBits = decode(dc, supplier);
		if (!supplier.canSupply(dcBits)) {
			return;
		} else {
			final int dcDiff = supplier.supply(dcBits);
			dst[dstOffset] = convertToSigned(dcDiff, dcBits);
		}

		for (int i = 1; i < dstLength;) {
			final int acBits = decode(ac, supplier);

			if (acBits == 0) { // End of block, zero out remaining elements
				for (int j = i; j < dstLength; ++j) {
					dst[j + dstOffset] = 0;
				}
				break;
			}

			if (acBits == 0xF0) { // Zero out 16 elements
				if (dstLength < i + 16) {
					throw new HuffmanException("Overflow",
							String.format("AC code 0xF0 detected. Unable to zero %d elements at position %d of %d", 16, i, dstLength));
				}

				for (final int l = i + 16; i < l; ++i) {
					dst[i + dstOffset] = 0;
				}
				continue;
			}

			final int msbAC = acBits >>> 4 & 0xF;
			if (dstLength < i + msbAC) {
				throw new HuffmanException("Overflow", String.format("AC high bits set. Unable to zero %d elements at position %d of %d", msbAC, i, dstLength));
			}

			for (final int l = i + msbAC; i < l; ++i) {
				dst[i + dstOffset] = 0;
			}

			final int lsbAC = acBits & 0xF;

			if (!supplier.canSupply(lsbAC)) {
				return;
			}

			final int acValue = supplier.supply(lsbAC);
			dst[i++ + dstOffset] = convertToSigned(acValue, lsbAC);
		}

		return;
	}

	private static void assertNotOutOfBounds(String argumentName, int value, int lowerBound, int upperBound) {
		if (value < lowerBound || upperBound < value) {
			throw new IllegalArgumentException(String.format("%s : %d is not within [%d; %d]", argumentName, value, lowerBound, upperBound));
		}
	}

	private static int decode(HuffmanTable decoder, BitSupply supplier) {
		final int maxLength = decoder.getDecodeMaxLength();
		final int minLength = decoder.getDecodeMinLength();

		if (minLength == 0 || maxLength == 0) {
			return 0;
		}

		int word = 0;
		int wordLength = 0;
		int numberOfBits = minLength;

		do {
			if (!decoder.hasDecodingForWordOfLength(numberOfBits)) {
				numberOfBits += 1;
				continue;
			}

			final int diff = numberOfBits - wordLength;
			if (!supplier.canSupply(diff)) {
				return 0;
			}

			word = word << diff | supplier.supply(diff);
			wordLength += diff;
			final var huffVal = decoder.decode(word, wordLength);
			if (huffVal == null) {
				numberOfBits += 1;
				continue;
			}
			return huffVal.decodedWord;

		} while (numberOfBits <= maxLength);

		if (numberOfBits > maxLength) {
			final String encodedWord = String.format("%" + wordLength + "s", Integer.toBinaryString(word)).replaceAll(" ", "0");
			throw new HuffmanException("Decoding not found", String.format(
					"Table contains words between %d and %d bits. Word '%s' with length %d has no match.", minLength, maxLength, encodedWord, wordLength));
		}
		return 0;
	}

	private static int convertToSigned(int data, int nBits) {
		int exData = 1 << nBits - 1;
		if (data < exData) {
			exData = (-1 << nBits) + 1;
			data = data + exData;
		}
		return data;
	}

}