package nexusvault.format.tex.jpg;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

final class HuffmanTable {

	public static class HuffmanValue {
		/** number of used bits in the encodedWord, starting with the lsb */
		public final int nBitsEncoded;
		public final int encodedWord;
		public final int decodedWord;

		public HuffmanValue(int encodedWord, int nBitsEncoded, int decodedWord) {
			super();
			this.nBitsEncoded = nBitsEncoded;
			this.encodedWord = encodedWord;
			this.decodedWord = decodedWord;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("HuffmanValue [nBitsEncoded=");
			builder.append(nBitsEncoded);
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
			result = (prime * result) + nBitsEncoded;
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
			if (nBitsEncoded != other.nBitsEncoded) {
				return false;
			}
			return true;
		}

	}

	protected static class HuffmanKey implements Comparable<HuffmanKey> {
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

	private final Map<HuffmanTable.HuffmanKey, HuffmanTable.HuffmanValue> mapping;
	final DHTPackage table;

	public HuffmanTable(DHTPackage table) {
		this.table = table;
		mapping = new TreeMap<>();

		int encodedWord = 0;
		for (int idx = 0; idx < table.codes.length; ++idx) {
			final int encodedLength = idx + 1;
			if (table.codes[idx].length > (1 << (idx + 1))) {
				throw new IllegalArgumentException(String.format(
						"DHTPackage error. Package contains %1$d words with a length of %2$d. This lengths only supports encoding for up to %3$d words.",
						table.codes[idx].length, encodedLength, 1 << (encodedLength)));
			}

			for (int nIdx = 0; nIdx < table.codes[idx].length; ++nIdx) {
				final int decodedWord = table.codes[idx][nIdx];
				final HuffmanKey key = new HuffmanKey(encodedWord, encodedLength);
				mapping.put(key, new HuffmanValue(encodedWord, encodedLength, decodedWord));
				encodedWord = encodedWord + 1;
			}
			encodedWord = encodedWord << 1;
		}
	}

	public boolean hasWordOfLength(int nBits) {
		if ((nBits < 0) || (table.codes.length < nBits)) {
			return false;
		}
		return table.codes[nBits - 1].length != 0;
	}

	public HuffmanTable.HuffmanValue decode(int code, int length) {
		final HuffmanTable.HuffmanValue key = mapping.get(new HuffmanKey(code, length));
		return key;
	}

	public int getMinLength() {
		for (int i = 0; i < table.codes.length; ++i) {
			if (table.codes[i].length != 0) {
				return i + 1;
			}
		}
		return 0;
	}

	public int getMaxLength() {
		for (int i = table.codes.length - 1; 0 <= i; --i) {
			if (table.codes[i].length != 0) {
				return i + 1;
			}
		}
		return 0;
	}

	public void asFormatedString() {
		asFormatedString(System.out);
	}

	public void asFormatedString(PrintStream out) {
		out.println("Huffman Table: Total number of codes: " + mapping.size());
		final String strPadding = "%" + mapping.size() + "s";
		for (final HuffmanKey key : mapping.keySet().stream().sorted().collect(Collectors.toList())) {
			final HuffmanValue value = mapping.get(key);
			final String binary = String.format("%" + key.length + "s", Integer.toBinaryString(key.bits)).replaceAll(" ", "0");
			final String paddedBinary = String.format(strPadding, binary);
			final String toPrint = String.format("Key(%02d) %s -> Value: 0x%02X", key.length, paddedBinary, value.decodedWord);
			out.println(toPrint);
		}
	}

}