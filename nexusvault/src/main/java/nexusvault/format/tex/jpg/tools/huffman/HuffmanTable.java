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

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class HuffmanTable {

	public static final class HuffmanValue {
		/** number of used bits in the encodedWord, starting with the lsb */
		public final int encodedWordBitLength;
		public final int encodedWord;

		/** Only the 16 ls-bits are used */
		public final int decodedWord;

		public HuffmanValue(int encodedWord, int encodedWordBitLength, int decodedWord) {
			super();
			this.encodedWordBitLength = encodedWordBitLength;
			this.encodedWord = encodedWord;
			this.decodedWord = decodedWord;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("HuffmanValue [encodedWordBitLength=");
			builder.append(this.encodedWordBitLength);
			builder.append(", encodedWord=");
			builder.append(this.encodedWord);
			builder.append(", decodedWord=");
			builder.append(this.decodedWord);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.decodedWord;
			result = prime * result + this.encodedWord;
			result = prime * result + this.encodedWordBitLength;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final HuffmanValue other = (HuffmanValue) obj;
			if (this.decodedWord != other.decodedWord) {
				return false;
			}
			if (this.encodedWord != other.encodedWord) {
				return false;
			}
			if (this.encodedWordBitLength != other.encodedWordBitLength) {
				return false;
			}
			return true;
		}

	}

	protected static final class HuffmanKey implements Comparable<HuffmanKey> {
		public final int bits;
		public final int length;

		public HuffmanKey(int bits, int length) {
			super();
			this.bits = bits;
			this.length = length;
		}

		@Override
		public String toString() {
			String bin = String.format("%" + this.length + "s", Integer.toBinaryString(this.bits));
			bin = bin.replaceAll(" ", "0");
			final StringBuilder b = new StringBuilder();
			b.append("[Key(").append(this.length).append("): ").append(bin).append("]");
			return b.toString();
		}

		public boolean isMatch(int bits, int length) {
			return this.bits == bits && this.length == length;
		}

		public boolean isMatch(HuffmanKey key) {
			return this.isMatch(key.bits, key.length);
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (o instanceof HuffmanKey) {
				return isMatch((HuffmanKey) o);
			}
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.bits;
			result = prime * result + this.length;
			return result;
		}

		@Override
		public int compareTo(HuffmanKey o) {
			if (this.length != o.length) {
				return this.length - o.length;
			} else {
				return this.bits - o.bits;
			}
		}

	}

	private final Map<HuffmanTable.HuffmanKey, HuffmanTable.HuffmanValue> decodeMapping = new TreeMap<>();
	private final Map<HuffmanTable.HuffmanKey, HuffmanTable.HuffmanValue> encodeMapping = new TreeMap<>();
	private final int[][] codes = new int[16][];

	public HuffmanTable(int[] numberOfValues, int[] values) {
		// sort values in categories
		int pos = 0;
		for (int i = 0; i < numberOfValues.length; ++i) {
			this.codes[i] = new int[numberOfValues[i]];
			for (int j = 0; j < numberOfValues[i]; ++j) {
				this.codes[i][j] = values[pos++];
			}
		}

		buildHuffmanTree();
	}

	private void buildHuffmanTree() {
		// decoding
		int encodedWord = 0;
		for (int idx = 0; idx < this.codes.length; ++idx) {
			final int encodedLength = idx + 1;
			if (this.codes[idx].length > 1 << idx + 1) {
				throw new IllegalArgumentException(
						String.format("Code error. Step %1$d contains %2$d words. With a bit length of %3$d only %4$d words are supported", idx + 1,
								this.codes[idx].length, encodedLength, 1 << encodedLength));
			}

			for (int nIdx = 0; nIdx < this.codes[idx].length; ++nIdx) {
				final int decodedWord = this.codes[idx][nIdx];
				final var key = new HuffmanKey(encodedWord, encodedLength);
				this.decodeMapping.put(key, new HuffmanValue(encodedWord, encodedLength, decodedWord));
				encodedWord += 1;
			}
			encodedWord <<= 1;
		}

		// encoding
		for (final var value : this.decodeMapping.values()) {
			final var key = new HuffmanKey(value.decodedWord, Integer.SIZE);
			this.encodeMapping.put(key, value);
		}
	}

	/**
	 * @param encodedWord
	 *            word which should be decoded
	 * @param bitLength
	 *            the number of bits used in <code>encodedWord</code>
	 * @return the decoding or null if no decoding is available for the <code>encodedWord</code>
	 */
	public HuffmanTable.HuffmanValue decode(int encodedWord, int bitLength) {
		final HuffmanTable.HuffmanValue decoding = this.decodeMapping.get(new HuffmanKey(encodedWord, bitLength));
		return decoding;
	}

	public HuffmanTable.HuffmanValue encode(int decodedWord) {
		final HuffmanTable.HuffmanValue encoding = this.encodeMapping.get(new HuffmanKey(decodedWord, Integer.SIZE));
		return encoding;
	}

	/**
	 * @param nBits
	 *            length of the word. Given in the number of used bits
	 * @return true when there is at least one decoding available
	 */
	public boolean hasDecodingForWordOfLength(int nBits) {
		if (nBits < 0 || this.codes.length < nBits) {
			return false;
		}
		return this.codes[nBits - 1].length != 0;
	}

	/**
	 * @return minimal number of bits needed for decoding
	 */
	public int getDecodeMinLength() {
		for (int i = 0; i < this.codes.length; ++i) {
			if (this.codes[i].length != 0) {
				return i + 1;
			}
		}
		return 0;
	}

	/**
	 * @return maximal number of bits which can be used for decoding
	 */
	public int getDecodeMaxLength() {
		for (int i = this.codes.length - 1; 0 <= i; --i) {
			if (this.codes[i].length != 0) {
				return i + 1;
			}
		}
		return 0;
	}

	public String getDecodingAsFormatedString() {
		final var builder = new StringBuilder();
		builder.append("Huffman Table: Total number of codes: ").append(this.decodeMapping.size()).append("\n");
		final String strPadding = "%" + this.decodeMapping.size() + "s";
		for (final HuffmanKey key : this.decodeMapping.keySet().stream().sorted().collect(Collectors.toList())) {
			final HuffmanValue value = this.decodeMapping.get(key);
			final String binary = String.format("%" + key.length + "s", Integer.toBinaryString(key.bits)).replaceAll(" ", "0");
			final String paddedBinary = String.format(strPadding, binary);
			final String toPrint = String.format("Key(%02d) %s -> Value: 0x%02X", key.length, paddedBinary, value.decodedWord);
			builder.append(toPrint).append("\n");
		}
		return builder.toString();
	}

}