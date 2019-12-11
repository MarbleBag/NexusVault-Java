package nexusvault.format.tex.jpg.tool;

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
			builder.append(encodedWordBitLength);
			builder.append(", encodedWord=");
			builder.append(encodedWord);
			builder.append(", decodedWord=");
			builder.append(decodedWord);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + decodedWord;
			result = (prime * result) + encodedWord;
			result = (prime * result) + encodedWordBitLength;
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
			if (decodedWord != other.decodedWord) {
				return false;
			}
			if (encodedWord != other.encodedWord) {
				return false;
			}
			if (encodedWordBitLength != other.encodedWordBitLength) {
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
			String bin = String.format("%" + length + "s", Integer.toBinaryString(bits));
			bin = bin.replaceAll(" ", "0");
			final StringBuilder b = new StringBuilder();
			b.append("[Key(").append(length).append("): ").append(bin).append("]");
			return b.toString();
		}

		public boolean isMatch(int bits, int length) {
			return (this.bits == bits) && (this.length == length);
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
			result = (prime * result) + bits;
			result = (prime * result) + length;
			return result;
		}

		@Override
		public int compareTo(HuffmanKey o) {
			if (length != o.length) {
				return length - o.length;
			} else {
				return bits - o.bits;
			}
		}

	}

	private final Map<HuffmanTable.HuffmanKey, HuffmanTable.HuffmanValue> decodeMapping;
	private final Map<HuffmanTable.HuffmanKey, HuffmanTable.HuffmanValue> encodeMapping;
	private final DHTPackage table;

	public HuffmanTable(DHTPackage table) {
		if (table == null) {
			throw new IllegalArgumentException("'table' must not be null");
		}

		this.table = table;

		decodeMapping = new TreeMap<>();
		encodeMapping = new TreeMap<>();

		buildDecodingMap();
		buildEncodingMap();
	}

	private void buildDecodingMap() {
		int encodedWord = 0;
		for (int idx = 0; idx < table.codes.length; ++idx) {
			final int encodedLength = idx + 1;
			if (table.codes[idx].length > (1 << (idx + 1))) {
				throw new IllegalArgumentException(String.format(
						"DHTPackage error. Package contains %1$d words with a length of %2$d. This length only supports encoding for up to %3$d words.",
						table.codes[idx].length, encodedLength, 1 << (encodedLength)));
			}

			for (int nIdx = 0; nIdx < table.codes[idx].length; ++nIdx) {
				final int decodedWord = table.codes[idx][nIdx];
				final HuffmanKey key = new HuffmanKey(encodedWord, encodedLength);
				decodeMapping.put(key, new HuffmanValue(encodedWord, encodedLength, decodedWord));
				encodedWord = encodedWord + 1;
			}
			encodedWord = encodedWord << 1;
		}
	}

	private void buildEncodingMap() {
		for (final var value : decodeMapping.values()) {
			final var key = new HuffmanKey(value.decodedWord, Integer.SIZE);
			encodeMapping.put(key, value);
		}
	}

	public DHTPackage getDHT() {
		return table;
	}

	/**
	 * @param encodedWord
	 * @param bitLength
	 *            the number of bits used in <code>encodedWord</code>
	 * @return the decoding or null if no decoding is available for the <code>encodedWord</code>
	 */
	public HuffmanTable.HuffmanValue decode(int encodedWord, int bitLength) {
		final HuffmanTable.HuffmanValue decoding = decodeMapping.get(new HuffmanKey(encodedWord, bitLength));
		return decoding;
	}

	public HuffmanTable.HuffmanValue encode(int decodedWord) {
		final HuffmanTable.HuffmanValue encoding = encodeMapping.get(new HuffmanKey(decodedWord, Integer.SIZE));
		return encoding;
	}

	/**
	 * @param nBits
	 *            length of the word. Given in the number of used bits
	 * @return true when there is at least one decoding available
	 */
	public boolean hasDecodingForWordOfLength(int nBits) {
		if ((nBits < 0) || (table.codes.length < nBits)) {
			return false;
		}
		return table.codes[nBits - 1].length != 0;
	}

	/**
	 * @return minimal number of bits needed for decoding
	 */
	public int getDecodeMinLength() {
		for (int i = 0; i < table.codes.length; ++i) {
			if (table.codes[i].length != 0) {
				return i + 1;
			}
		}
		return 0;
	}

	/**
	 * @return maximal number of bits which can be used for decoding
	 */
	public int getDecodeMaxLength() {
		for (int i = table.codes.length - 1; 0 <= i; --i) {
			if (table.codes[i].length != 0) {
				return i + 1;
			}
		}
		return 0;
	}

	public String getDecodingAsFormatedString() {
		final var builder = new StringBuilder();
		builder.append("Huffman Table: Total number of codes: ").append(decodeMapping.size()).append("\n");
		final String strPadding = "%" + decodeMapping.size() + "s";
		for (final HuffmanKey key : decodeMapping.keySet().stream().sorted().collect(Collectors.toList())) {
			final HuffmanValue value = decodeMapping.get(key);
			final String binary = String.format("%" + key.length + "s", Integer.toBinaryString(key.bits)).replaceAll(" ", "0");
			final String paddedBinary = String.format(strPadding, binary);
			final String toPrint = String.format("Key(%02d) %s -> Value: 0x%02X", key.length, paddedBinary, value.decodedWord);
			builder.append(toPrint).append("\n");
		}
		return builder.toString();
	}

}